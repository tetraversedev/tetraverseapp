package com.tetraverse.app

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tetraverse.app.game.AudioManager
import com.tetraverse.app.game.premiumSkins
import com.tetraverse.app.game.Skin
import com.tetraverse.app.screens.*
import com.tetraverse.app.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import com.solana.mobilewalletadapter.clientlib.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sol4k.Connection
import org.sol4k.PublicKey
import org.sol4k.Transaction
import org.sol4k.instruction.TransferInstruction
import org.sol4k.api.Commitment
import android.util.Base64

class MainActivity : ComponentActivity() {
    private lateinit var sender: ActivityResultSender
    private lateinit var audio: AudioManager

    // --- Config ---
    private val TREASURY_ADDRESS = "AaUtvduiu2DxBWEe9kNe74WMhMMf4qLGssVRfiafaC5N" // Real address
    private val RPC_URL = "https://api.mainnet-beta.solana.com" // Use standard public RPC
    private val connection = Connection(RPC_URL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        
        sender = ActivityResultSender(this)
        audio = AudioManager(this)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                
                // --- Persisted & Network State ---
                val prefs = remember { getSharedPreferences("tetra_prefs", MODE_PRIVATE) }
                var isWalletConnected by remember { mutableStateOf(false) }
                var walletAddress by remember { mutableStateOf("") }
                var latestSignature by remember { mutableStateOf(prefs.getString("last_sig", "") ?: "") }
                
                var selectedAvatarId by remember { 
                    mutableIntStateOf(prefs.getInt("avatar_id", 0)) 
                }

                // --- Ownership State ---
                var ownedSkinIds by remember {
                    mutableStateOf(prefs.getStringSet("owned_skins", setOf("0"))?.map { it.toInt() }?.toSet() ?: setOf(0))
                }
                var equippedSkinId by remember {
                    mutableIntStateOf(prefs.getInt("equipped_skin", 0))
                }
                
                // Sync to prefs
                LaunchedEffect(selectedAvatarId) {
                    prefs.edit().putInt("avatar_id", selectedAvatarId).apply()
                }
                LaunchedEffect(latestSignature) {
                    prefs.edit().putString("last_sig", latestSignature).apply()
                }
                LaunchedEffect(ownedSkinIds) {
                    prefs.edit().putStringSet("owned_skins", ownedSkinIds.map { it.toString() }.toSet()).apply()
                }
                LaunchedEffect(equippedSkinId) {
                    prefs.edit().putInt("equipped_skin", equippedSkinId).apply()
                }

                // --- Solana Logic ---
                val connectionIdentity = remember {
                    ConnectionIdentity(
                        identityUri = Uri.parse("https://tetraverse.vercel.app/"),
                        iconUri = Uri.parse("https://tetraverse.vercel.app/favicon.ico"),
                        identityName = "Tetraverse"
                    )
                }
                val walletAdapter = remember { MobileWalletAdapter(connectionIdentity) }

                fun connectSeekerWallet() {
                    scope.launch {
                        try {
                            val result = walletAdapter.transact(sender) {
                                val auth = authorize(
                                    identityUri = Uri.parse("https://tetraverse.vercel.app/"),
                                    iconUri = Uri.parse("https://tetraverse.vercel.app/favicon.ico"),
                                    identityName = "Tetraverse",
                                    chain = "solana:mainnet" // Mainnet
                                )
                                auth
                            }
                            
                            when (result) {
                                is TransactionResult.Success -> {
                                    val account = result.payload.accounts.firstOrNull()
                                    // Use sol4k PublicKey to convert ByteArray to Base58 String correctly
                                    walletAddress = account?.publicKey?.let { PublicKey(it).toString() } ?: ""
                                    
                                    isWalletConnected = true
                                    Toast.makeText(this@MainActivity, "Wallet Connected", Toast.LENGTH_SHORT).show()
                                }
                                is TransactionResult.Failure -> {
                                    Toast.makeText(this@MainActivity, "Connection Failed: ${result.e.message}", Toast.LENGTH_LONG).show()
                                }
                                is TransactionResult.NoWalletFound -> {
                                    Toast.makeText(this@MainActivity, "No Solana wallet found", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Connection Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                fun purchaseSkin(skin: Skin) {
                    if (!isWalletConnected) {
                        Toast.makeText(this@MainActivity, "Connect Wallet first!", Toast.LENGTH_SHORT).show()
                        return
                    }

                    scope.launch {
                        try {
                            Toast.makeText(this@MainActivity, "Building Transaction for ${skin.price}...", Toast.LENGTH_SHORT).show()
                            
                            // 1. Fetch blockhash from RPC (on IO thread)
                            val blockhash = withContext<String>(Dispatchers.IO) {
                                connection.getLatestBlockhash(Commitment.CONFIRMED)
                            }

                            val result = walletAdapter.transact(sender) {
                                // 2. Re-authorize
                                val auth = authorize(
                                    identityUri = Uri.parse("https://tetraverse.vercel.app/"),
                                    iconUri = Uri.parse("https://tetraverse.vercel.app/favicon.ico"),
                                    identityName = "Tetraverse",
                                    chain = "solana:mainnet" // Mainnet
                                )
                                
                                val userPubKey = PublicKey(auth.accounts.first().publicKey)
                                val receiverPubKey = PublicKey(TREASURY_ADDRESS)
                                
                                // Parse price string (e.g., "0.00001 SOL") to lamports
                                // We use a more robust way to parse decimal strings
                                val solValue = skin.price.split(" ")[0].toBigDecimal()
                                val lamports = (solValue * 1_000_000_000.toBigDecimal()).toLong()

                                // 3. Build Transfer Instruction
                                val instruction = TransferInstruction(
                                    from = userPubKey,
                                    to = receiverPubKey,
                                    lamports = lamports
                                )

                                // 4. Create Transaction (Legacy)
                                val transaction = Transaction(
                                    recentBlockhash = blockhash,
                                    instruction = instruction,
                                    feePayer = userPubKey
                                )

                                // 5. Add a dummy signature slot for MWA to fill
                                // Legacy Transaction.addSignature does not verify the signature
                                transaction.addSignature(org.sol4k.Base58.encode(ByteArray(64)))

                                // 6. Request Wallet to Sign and Send
                                signAndSendTransactions(arrayOf(transaction.serialize()))
                            }

                            when (result) {
                                is TransactionResult.Success -> {
                                    val signature = result.payload.signatures.firstOrNull()?.let {
                                        Base64.encodeToString(it, Base64.NO_WRAP)
                                    } ?: "Unknown"
                                    
                                    latestSignature = signature
                                    ownedSkinIds = ownedSkinIds + skin.id
                                    equippedSkinId = skin.id

                                    Toast.makeText(this@MainActivity, "Purchase Successful! Sig: ${signature.take(8)}...", Toast.LENGTH_LONG).show()
                                }
                                is TransactionResult.Failure -> {
                                    Toast.makeText(this@MainActivity, "Transaction Failed: ${result.e.message}", Toast.LENGTH_LONG).show()
                                }
                                is TransactionResult.NoWalletFound -> {
                                    Toast.makeText(this@MainActivity, "No Solana wallet found", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                NavHost(navController = navController, startDestination = "main_menu") {
                    composable("main_menu") {
                        LaunchedEffect(Unit) { audio.playLobbyMusic() }
                        val equippedColors = premiumSkins.find { it.id == equippedSkinId }?.colors
                        MainMenuScreen(
                            onStartGame = { navController.navigate("game") },
                            onOpenShop = { navController.navigate("shop") },
                            onOpenLeaderboard = { navController.navigate("leaderboard") },
                            onOpenProfile = { 
                                selectedAvatarId = (selectedAvatarId + 1) % avatarList.size
                            },
                            onConnectWallet = { connectSeekerWallet() },
                            isWalletConnected = isWalletConnected,
                            walletAddress = walletAddress,
                            latestSignature = latestSignature,
                            selectedAvatarId = selectedAvatarId,
                            equippedColor = equippedColors?.firstOrNull()
                        )
                    }
                    composable("game") {
                        val equippedColors = premiumSkins.find { it.id == equippedSkinId }?.colors
                        GameScreen(
                            audioManager = audio,
                            overrideColors = equippedColors,
                            walletAddress = walletAddress,
                            onBack = { 
                                audio.playLobbyMusic()
                                navController.popBackStack() 
                            }
                        )
                    }
                    composable("shop") {
                        ShopScreen(
                            currentAvatarId = selectedAvatarId,
                            ownedSkinIds = ownedSkinIds,
                            equippedSkinId = equippedSkinId,
                            onAvatarSelected = { id -> selectedAvatarId = id },
                            onEquipSkin = { id -> equippedSkinId = id },
                            onBack = { navController.popBackStack() },
                            onPurchase = { skin -> purchaseSkin(skin) }
                        )
                    }
                    composable("leaderboard") {
                        LeaderboardScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        audio.resumeBGM()
    }

    override fun onPause() {
        super.onPause()
        audio.pauseBGM()
    }

    override fun onDestroy() {
        super.onDestroy()
        audio.stopBGM()
    }
}

package com.example.gk_ltdd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gk_ltdd.model.User
import com.example.gk_ltdd.viewmodel.AuthViewModel
import com.example.gk_ltdd.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ASSIGNED_BUT_NOT_USED_WARNING")
@Composable
fun UserListScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val uiState by userViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    var deleteDialogUser by remember { mutableStateOf<User?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        authViewModel.refreshAdminStatus()
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); userViewModel.clearMessage() }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); userViewModel.clearMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quản lý người dùng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(authState.user?.email ?: "", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0), titleContentColor = Color.White),
                actions = {
                    Surface(shape = RoundedCornerShape(20.dp), color = if (authState.isAdmin) Color(0xFFFFD54F) else Color(0xFF81C784)) {
                        Text(
                            if (authState.isAdmin) "Admin" else "User",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }) { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White) }
                }
            )
        },
        floatingActionButton = {
            if (authState.isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("add_user") },
                    icon = { Icon(Icons.Default.PersonAdd, null) },
                    text = { Text("Thêm user") },
                    containerColor = Color(0xFF1565C0), contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.users.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PeopleOutline, null, modifier = Modifier.size(80.dp), tint = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    Text("Chưa có người dùng nào", color = Color.Gray)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Text("Tổng cộng: ${uiState.users.size} người dùng", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp)) }
                    items(uiState.users, key = { it.id }) { user ->
                        UserCard(
                            user = user,
                            isAdmin = authState.isAdmin,
                            onEdit = { navController.navigate("edit_user/${user.id}") },
                            onDelete = { deleteDialogUser = user }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    deleteDialogUser?.let { user ->
        AlertDialog(
            onDismissRequest = {
                @Suppress("ASSIGNED_BUT_NOT_USED_WARNING")
                deleteDialogUser = null
            },
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFE53935)) },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa \"${user.username}\"? Không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.deleteUser(user.id)
                        @Suppress("ASSIGNED_BUT_NOT_USED_WARNING")
                        deleteDialogUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Xóa") }
            },
            dismissButton = { OutlinedButton(onClick = {
                @Suppress("ASSIGNED_BUT_NOT_USED_WARNING")
                deleteDialogUser = null
            }) { Text("Hủy") } }
        )
    }
}

@Composable
fun UserCard(user: User, isAdmin: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(3.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                if (user.imageUrl.isNotEmpty()) {
                    AsyncImage(model = user.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(30.dp), tint = Color(0xFF1565C0))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.username.ifEmpty { "Không tên" }, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = if (user.role == "admin") Color(0xFFFFECB3) else Color(0xFFE8F5E9)) {
                        Text(
                            if (user.role == "admin") "Admin" else "User",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = if (user.role == "admin") Color(0xFFE65100) else Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(user.email.ifEmpty { "Không có email" }, fontSize = 13.sp, color = Color.Gray)
                if (user.fileName.isNotEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                        Spacer(Modifier.width(3.dp))
                        Text(user.fileName, fontSize = 12.sp, color = Color(0xFF1565C0))
                    }
                }
            }
            if (isAdmin) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, null, tint = Color(0xFF1565C0)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935)) }
            }
        }
    }
}
package com.example.gk_ltdd.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gk_ltdd.model.User
import com.example.gk_ltdd.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditUserScreen(navController: NavController, userId: String?, viewModel: UserViewModel = viewModel()) {
    val isEditMode = userId != null
    val uiState by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("user") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }
    var existingImageUrl by remember { mutableStateOf("") }
    var existingFileUrl by remember { mutableStateOf("") }
    var existingFileName by remember { mutableStateOf("") }
    var isDataLoaded by remember { mutableStateOf(!isEditMode) }

    LaunchedEffect(userId) {
        if (isEditMode && userId != null) {
            val user = viewModel.getUserById(userId)
            user?.let {
                username = it.username; password = it.password; email = it.email
                selectedRole = it.role; existingImageUrl = it.imageUrl
                existingFileUrl = it.fileUrl; existingFileName = it.fileName; fileName = it.fileName
                isDataLoaded = true
            }
        }
    }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) { viewModel.clearMessage(); navController.popBackStack() }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { imageUri = it } }
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { fileUri = it; fileName = it.lastPathSegment ?: "file" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Chỉnh sửa người dùng" else "Thêm người dùng", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton({ navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0), titleContentColor = Color.White)
            )
        }
    ) { padding ->
        if (!isDataLoaded) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar picker
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape)
                    .background(Color(0xFFE3F2FD))
                    .border(2.dp, Color(0xFF1565C0), CircleShape)
                    .clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageUri != null -> AsyncImage(imageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    existingImageUrl.isNotEmpty() -> AsyncImage(existingImageUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else -> Icon(Icons.Default.AddAPhoto, null, Modifier.size(36.dp), tint = Color(0xFF1565C0))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Nhấn để chọn ảnh", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(20.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Thông tin cơ bản", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1565C0))
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(username, { username = it }, label = { Text("Tên người dùng *") },
                        leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(email, { email = it }, label = { Text("Email *") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        password, { password = it },
                        label = { Text(if (isEditMode) "Mật khẩu (để trống nếu không đổi)" else "Mật khẩu *") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = { IconButton({ passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    ExposedDropdownMenuBox(roleExpanded, { roleExpanded = !roleExpanded }) {
                        OutlinedTextField(
                            if (selectedRole == "admin") "Quản trị viên" else "Người dùng", {},
                            readOnly = true, label = { Text("Vai trò") },
                            leadingIcon = { Icon(Icons.Default.Shield, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(roleExpanded, { roleExpanded = false }) {
                            DropdownMenuItem(text = { Text("Người dùng") }, onClick = { selectedRole = "user"; roleExpanded = false })
                            DropdownMenuItem(text = { Text("Quản trị viên") }, onClick = { selectedRole = "admin"; roleExpanded = false })
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tệp đính kèm", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1565C0))
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { fileLauncher.launch("*/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.AttachFile, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Chọn tệp đính kèm")
                    }
                    val displayName = if (fileUri != null) fileName else existingFileName.ifEmpty { null }
                    displayName?.let {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE3F2FD)).padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.InsertDriveFile, null, Modifier.size(20.dp), tint = Color(0xFF1565C0))
                            Spacer(Modifier.width(8.dp))
                            Text(it, fontSize = 13.sp, color = Color(0xFF1565C0), modifier = Modifier.weight(1f))
                            if (fileUri != null) Text("(Mới)", fontSize = 11.sp, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }

            uiState.error?.let {
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Text(it, Modifier.padding(12.dp), color = Color(0xFFB71C1C), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val user = User(id = userId ?: "", username = username, password = password,
                        email = email, role = selectedRole, imageUrl = existingImageUrl,
                        fileUrl = existingFileUrl, fileName = existingFileName)
                    if (isEditMode) viewModel.updateUser(user, imageUri, fileUri, fileName)
                    else viewModel.addUser(user, imageUri, fileUri, fileName)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isLoading && username.isNotBlank() && email.isNotBlank() && (isEditMode || password.isNotBlank()),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else {
                    Icon(if (isEditMode) Icons.Default.Save else Icons.Default.PersonAdd, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditMode) "Lưu thay đổi" else "Thêm người dùng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
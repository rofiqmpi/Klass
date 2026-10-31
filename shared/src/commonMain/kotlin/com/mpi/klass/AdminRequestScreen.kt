package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.PersonIcon
import kotlinx.coroutines.launch

@Composable
fun AdminRequestScreen(
    adminEmail: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val coroutineScope = rememberCoroutineScope()
    val requests = remember { mutableStateListOf<LoginRequest>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Approval dialog state
    var showApprovalDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<LoginRequest?>(null) }

    fun loadRequests() {
        isLoading = true
        coroutineScope.launch {
            val fetched = RoleConfig.fetchPendingRequests()
            requests.clear()
            requests.addAll(fetched)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadRequests() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "লগিন রিকোয়েস্ট",
                fontSize = 18.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
            OutlinedButton(
                onClick = { loadRequests() },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
            ) {
                Text(
                    text = "রিফ্রেশ",
                    fontFamily = hindSiliguri,
                    fontSize = 12.sp,
                    color = Color(0xFF0284C7),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color(0xFF0284C7))
            }
        } else if (requests.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardBg,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "কোনো রিকোয়েস্ট নেই",
                    fontSize = 14.sp,
                    fontFamily = hindSiliguri,
                    color = subTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            }
        } else {
            // Filter tabs
            val pendingList = requests.filter { it.status == RequestStatus.PENDING.name }
            val approvedList = requests.filter { it.status == RequestStatus.APPROVED.name }
            val rejectedList = requests.filter { it.status == RequestStatus.REJECTED.name }

            if (pendingList.isNotEmpty()) {
                StatusSectionHeader("অপেক্ষমান রিকোয়েস্ট (${pendingList.size})", Color(0xFFF59E0B), hindSiliguri)
                Spacer(modifier = Modifier.height(8.dp))
                pendingList.forEach { req ->
                    RequestCard(
                        request = req,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        cardBg = cardBg,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        onApprove = {
                            selectedRequest = req
                            showApprovalDialog = true
                        },
                        onReject = {
                            coroutineScope.launch {
                                RoleConfig.rejectLoginRequest(req.email, adminEmail)
                                loadRequests()
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (approvedList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                StatusSectionHeader("অনুমোদিত (${approvedList.size})", Color(0xFF10B981), hindSiliguri)
                Spacer(modifier = Modifier.height(8.dp))
                approvedList.forEach { req ->
                    RequestCard(
                        request = req,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        cardBg = cardBg,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        showActions = false,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (rejectedList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                StatusSectionHeader("প্রত্যাখ্যাত (${rejectedList.size})", Color(0xFFEF4444), hindSiliguri)
                Spacer(modifier = Modifier.height(8.dp))
                rejectedList.forEach { req ->
                    RequestCard(
                        request = req,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        cardBg = cardBg,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        showActions = false,
                        onApprove = {
                            // Re-approve a rejected request
                            selectedRequest = req
                            showApprovalDialog = true
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        errorMsg?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                fontSize = 13.sp,
                fontFamily = hindSiliguri,
                color = Color(0xFFEF4444),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // Approval Dialog
    if (showApprovalDialog && selectedRequest != null) {
        ApprovalDialog(
            request = selectedRequest!!,
            adminEmail = adminEmail,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = {
                showApprovalDialog = false
                selectedRequest = null
            },
            onApprove = { allowedFeatures ->
                coroutineScope.launch {
                    val result = RoleConfig.approveLoginRequest(
                        requestEmail = selectedRequest!!.email,
                        adminEmail = adminEmail,
                        allowedFeatures = allowedFeatures,
                    )
                    showApprovalDialog = false
                    selectedRequest = null
                    if (result.isSuccess) {
                        loadRequests()
                    } else {
                        errorMsg = "অনুমোদন দেওয়া যায়নি। পুনরায় চেষ্টা করুন।"
                    }
                }
            },
        )
    }
}

@Composable
private fun StatusSectionHeader(text: String, color: Color, hindSiliguri: FontFamily) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = hindSiliguri,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}

@Composable
private fun RequestCard(
    request: LoginRequest,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
    showActions: Boolean = true,
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
) {
    val statusColor = when (request.status) {
        RequestStatus.APPROVED.name -> Color(0xFF10B981)
        RequestStatus.REJECTED.name -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }
    val statusLabel = when (request.status) {
        RequestStatus.APPROVED.name -> "অনুমোদিত"
        RequestStatus.REJECTED.name -> "প্রত্যাখ্যাত"
        else -> "অপেক্ষমান"
    }
    val roleLabel = when (request.role) {
        "TEACHER" -> "শিক্ষক"
        "STUDENT" -> "শিক্ষার্থী"
        "ADMIN" -> "এডমিন"
        else -> request.role
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0284C7).copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = PersonIcon,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = request.name.ifBlank { "অজানা ব্যবহারকারী" },
                            fontSize = 14.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                        Text(
                            text = request.email,
                            fontSize = 11.sp,
                            color = subTextColor,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF6366F1).copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = roleLabel,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }

            if (showActions && request.status == RequestStatus.PENDING.name) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { onApprove?.invoke() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(imageVector = CheckIcon, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "অনুমোদন", fontFamily = hindSiliguri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onReject?.invoke() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    ) {
                        Icon(imageVector = CloseIcon, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "প্রত্যাখ্যান", fontFamily = hindSiliguri, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            } else if (!showActions && request.status == RequestStatus.REJECTED.name) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onApprove?.invoke() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White,
                    ),
                ) {
                    Text(text = "পুনরায় অনুমোদন দিন", fontFamily = hindSiliguri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ApprovalDialog(
    request: LoginRequest,
    adminEmail: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
    onApprove: (List<String>) -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    // Available features based on role
    val allFeatures = when (request.role) {
        "TEACHER" -> listOf("রুটিন দেখা", "নোটিশ দেওয়া", "ফলাফল আপলোড", "উপস্থিতি নেওয়া")
        "STUDENT" -> listOf("রুটিন দেখা", "নোটিশ দেখা", "ফলাফল দেখা", "উপস্থিতি দেখা")
        else -> emptyList()
    }

    val selectedFeatures = remember { mutableStateListOf<String>().also { it.addAll(allFeatures) } }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            shadowElevation = 16.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "অনুমোদন দিন",
                    fontSize = 18.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${request.name} (${request.email})",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (allFeatures.isNotEmpty()) {
                    Text(
                        text = "অনুমতি দিন (যা চাইলে বন্ধ রাখুন):",
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    allFeatures.forEach { feature ->
                        val isSelected = selectedFeatures.contains(feature)
                        Surface(
                            onClick = {
                                if (isSelected) selectedFeatures.remove(feature)
                                else selectedFeatures.add(feature)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.12f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF10B981).copy(alpha = 0.4f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (isSelected) CheckIcon else CloseIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feature,
                                    fontSize = 13.sp,
                                    fontFamily = hindSiliguri,
                                    color = textColor,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(text = "বাতিল", fontFamily = hindSiliguri, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { onApprove(selectedFeatures.toList()) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    ) {
                        Text(text = "অনুমোদন দিন", fontFamily = hindSiliguri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

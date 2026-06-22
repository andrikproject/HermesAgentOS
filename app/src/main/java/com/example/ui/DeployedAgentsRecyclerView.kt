package com.example.ui

import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.RecyclerView
import com.example.data.AgentSession
import com.example.ui.theme.*

class DeployedAgentAdapter(
    private var agentList: List<AgentSession>,
    private val activeSessionId: Int?,
    private val isGenerating: Boolean,
    private val onAgentClick: (AgentSession) -> Unit,
    private val onPingAgent: (AgentSession) -> Unit,
    private val onDeleteAgent: (AgentSession) -> Unit
) : RecyclerView.Adapter<DeployedAgentAdapter.AgentViewHolder>() {

    class AgentViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView) {
        init {
            composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgentViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return AgentViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: AgentViewHolder, position: Int) {
        val session = agentList[position]
        val isActive = session.id == activeSessionId
        
        val status = when {
            isActive && isGenerating -> "EXECUTING"
            isActive -> "ACTIVE"
            position % 3 == 0 -> "ONLINE"
            position % 3 == 1 -> "STANDBY"
            else -> "IDLE"
        }
        
        holder.composeView.setContent {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .border(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) CyberTeal else SlateMedium,
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) SlateCore else Color(0xFF090909)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.Circle else Icons.Default.Adjust,
                                contentDescription = "Core indicator",
                                tint = if (isActive) CyberTeal else TextSlateMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CORE-0${session.id}: ${session.name}",
                                color = if (isActive) Color.White else TextSlateMain,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (status) {
                                        "EXECUTING" -> GridGreen.copy(alpha = 0.2f)
                                        "ACTIVE" -> CyberTeal.copy(alpha = 0.2f)
                                        "ONLINE" -> Color(0xFF1E293B)
                                        "STANDBY" -> AlertAmber.copy(alpha = 0.2f)
                                        else -> SlateMedium
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (status == "EXECUTING") {
                                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "alpha"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GridGreen.copy(alpha = alpha))
                                    )
                                }
                                Text(
                                    text = status,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = when (status) {
                                        "EXECUTING" -> GridGreen
                                        "ACTIVE" -> CyberTeal
                                        "ONLINE" -> TextSlateMain
                                        "STANDBY" -> AlertAmber
                                        else -> TextSlateMuted
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PERSONA PROFILE:",
                                color = TextSlateMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = session.agentPersona,
                                color = TextSlateMain,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NEURAL COGNITIVE MODEL:",
                                color = TextSlateMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = session.agentModel,
                                color = HermesOrange,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAgentClick(session) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) CyberTeal.copy(alpha = 0.15f) else SlateMedium,
                                contentColor = if (isActive) CyberTeal else TextSlateMain
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.3f),
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Power,
                                contentDescription = "Deploy",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isActive) "ACTIVE CORE" else "MOUNT CORE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onPingAgent(session) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SlateMedium,
                                contentColor = HermesOrange
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "Ping",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PING TRACE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { onDeleteAgent(session) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateMedium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Core",
                                tint = ErrorRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = agentList.size

    fun updateData(newList: List<AgentSession>) {
        agentList = newList
        notifyDataSetChanged()
    }
}

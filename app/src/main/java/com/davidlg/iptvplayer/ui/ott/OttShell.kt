package com.davidlg.iptvplayer.ui.ott

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidlg.iptvplayer.MainViewModel
import com.davidlg.iptvplayer.OttTab
import com.davidlg.iptvplayer.R
import com.davidlg.iptvplayer.Screen
import com.davidlg.iptvplayer.ui.Poppins
import com.davidlg.iptvplayer.ui.dlClickable
import com.davidlg.iptvplayer.ui.profiles.ProfileAvatar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shell OTT nativo: navegación adaptativa, reloj, selector de perfiles y ajustes. */
@Composable
fun OttShell(vm: MainViewModel) {
    Column(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0B1728), Color(0xFF070D18), Color(0xFF050A12)))
        )
    ) {
        OttTopBar(vm)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (vm.ottTab) {
                OttTab.Start -> OttStart(vm)
                OttTab.Live -> OttLive(vm, "channels")
                OttTab.Epg -> OttLive(vm, "epg")
                OttTab.Movie -> OttVod(vm, "movie", "Películas")
                OttTab.Series -> OttVod(vm, "series", "Series")
                OttTab.Anime -> OttVod(vm, "anime", "Anime")
                OttTab.Fav -> OttVod(vm, "fav", "Mi lista")
            }
        }
        if (LocalConfiguration.current.screenWidthDp < 600) OttPhoneBottomBar(vm)
    }
}

@Composable
private fun OttPhoneBottomBar(vm: MainViewModel) {
    Row(
        Modifier.fillMaxWidth().height(66.dp).background(Color(0xF207101D)).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val phoneTabs = listOf(OttTab.Start, OttTab.Live, OttTab.Epg, OttTab.Movie, OttTab.Series, OttTab.Fav)
        for (tab in phoneTabs) {
            val def = TABS.first { it.tab == tab }
            val selected = vm.ottTab == tab
            Column(
                Modifier.weight(1f).fillMaxHeight().dlClickable(RoundedCornerShape(10.dp), Color.White, 1.dp, 1f) { vm.ottTab = tab },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                SvgIcon(def.ico, 20.dp, if (selected) OttGold else OttText.copy(alpha = 0.68f), 1.8f)
                Text(def.label, color = if (selected) OttGold else OttText.copy(alpha = 0.72f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, fontFamily = Poppins, maxLines = 1)
            }
        }
    }
}

private data class TabDef(val tab: OttTab, val label: String, val ico: List<String>)

private val TABS = listOf(
    TabDef(OttTab.Start, "Inicio", Ico.home),
    TabDef(OttTab.Live, "TV en vivo", Ico.live),
    TabDef(OttTab.Epg, "EPG", Ico.tv),
    TabDef(OttTab.Movie, "Películas", Ico.movie),
    TabDef(OttTab.Series, "Series", Ico.series),
    TabDef(OttTab.Anime, "Anime", Ico.anime),
    TabDef(OttTab.Fav, "Mi lista", Ico.fav),
)

@Composable
private fun OttTopBar(vm: MainViewModel) {
    val profile by vm.activeProfile.collectAsState()
    val profiles by vm.profiles.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val filter by vm.sourceFilter.collectAsState()
    val width = LocalConfiguration.current.screenWidthDp
    val phone = width < 600
    val tablet = width in 600..899
    var now by remember { mutableStateOf(Date()) }
    var profileMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(1000) } }

    val srcName = when {
        playlists.isEmpty() -> "Sin listas"
        filter == "all" -> "Todas las listas · ${playlists.size}"
        else -> playlists.firstOrNull { it.id == filter }?.name ?: "Todas las listas"
    }
    val glass = Color(0xF20A111E)
    val horizontalInset = when { phone -> 12.dp; tablet -> 18.dp; else -> 34.dp }

    Column(
        Modifier.fillMaxWidth().background(glass)
            .border(width = 1.dp, color = Color(0x1AFFFFFF), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = horizontalInset, vertical = if (phone) 7.dp else 10.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(if (phone) 44.dp else 58.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.logo_davidlg), "DAVIDLG IPTV",
                Modifier.width(if (phone) 122.dp else if (tablet) 148.dp else 176.dp).height(if (phone) 42.dp else 54.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.weight(1f))

            if (!phone) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = if (tablet) 12.dp else 18.dp)) {
                    Text(SimpleDateFormat("EEE, d MMM", ES).format(now).uppercase(), color = OttGoldDim, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text(SimpleDateFormat("HH:mm:ss", Locale.US).format(now), color = Color.White, fontSize = if (tablet) 15.sp else 19.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                }
            }

            if (!phone && !tablet) {
                Box(
                    Modifier.width(190.dp).height(42.dp).clip(RoundedCornerShape(13.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0x33168BFF), Color(0x0DFFFFFF))))
                        .border(1.dp, Color(0x55168BFF), RoundedCornerShape(13.dp))
                        .dlClickable(RoundedCornerShape(13.dp), OttGold, 2.dp, 1.02f) { vm.cycleSource() }
                        .padding(horizontal = 11.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Column {
                        Text("FUENTE ACTIVA", color = OttGoldDim, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        Text(srcName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Box {
                Row(
                    Modifier.padding(start = if (phone) 0.dp else 12.dp).clip(RoundedCornerShape(14.dp))
                        .background(Color(0x14FFFFFF)).border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                        .dlClickable(RoundedCornerShape(14.dp), OttGold, 2.dp, 1.04f) { profileMenu = true }
                        .padding(horizontal = if (phone) 4.dp else 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    ProfileAvatar(profile.av, if (phone) 34.dp else 38.dp)
                    if (!phone) {
                        Column {
                            Text(profile.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("PERFIL ACTIVO", color = OttGoldDim, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Text("⌄", color = OttText, fontSize = 17.sp)
                    }
                }
                DropdownMenu(expanded = profileMenu, onDismissRequest = { profileMenu = false }) {
                    profiles.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ProfileAvatar(item.av, 32.dp)
                                    Column {
                                        Text(item.name, fontWeight = FontWeight.SemiBold)
                                        if (item.id == profile.id) Text("Perfil actual", color = OttGold, fontSize = 10.sp)
                                    }
                                }
                            },
                            onClick = { profileMenu = false; if (item.id != profile.id) vm.selectProfile(item.id) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Administrar perfiles") },
                        onClick = { profileMenu = false; vm.showProfileSwitcher() },
                    )
                }
            }

            Box(
                Modifier.padding(start = 8.dp).size(if (phone) 38.dp else 42.dp)
                    .clip(RoundedCornerShape(13.dp)).background(Color(0x16FFFFFF))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(13.dp))
                    .dlClickable(RoundedCornerShape(13.dp), OttGold, 2.dp, 1.05f) { vm.screen = Screen.Settings },
                contentAlignment = Alignment.Center,
            ) { SvgIcon(Ico.gear, if (phone) 20.dp else 22.dp, OttText, 1.8f) }
        }

        if (phone) {
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp, start = 3.dp, end = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(SimpleDateFormat("EEE d MMM", ES).format(now).uppercase(), color = OttText.copy(alpha = .62f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("  ·  ", color = OttGoldDim, fontSize = 9.sp)
                Text(SimpleDateFormat("HH:mm:ss", Locale.US).format(now), color = OttGoldDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(srcName, color = OttGoldDim, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x1A168BFF))
                        .dlClickable(RoundedCornerShape(8.dp), OttGold, 1.dp, 1f) { vm.cycleSource() }.padding(horizontal = 8.dp, vertical = 5.dp))
            }
        }

        if (!phone) {
            Row(
                Modifier.fillMaxWidth().padding(top = if (tablet) 7.dp else 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(if (tablet) 5.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TABS.forEach { tab ->
                    val selected = vm.ottTab == tab.tab
                    Row(
                        Modifier.height(if (tablet) 36.dp else 42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Color(0x33168BFF) else Color.Transparent)
                            .then(if (selected) Modifier.border(1.dp, Color(0x77168BFF), RoundedCornerShape(12.dp)) else Modifier)
                            .dlClickable(RoundedCornerShape(12.dp), OttGold, 2.dp, 1.03f) { vm.ottTab = tab.tab }
                            .padding(horizontal = if (tablet) 10.dp else 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        SvgIcon(tab.ico, if (tablet) 15.dp else 17.dp, if (selected) Color.White else OttText.copy(alpha = .72f), 1.8f)
                        Text(tab.label, color = if (selected) Color.White else OttText.copy(alpha = .78f), fontSize = if (tablet) 11.sp else 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontFamily = Poppins, maxLines = 1)
                    }
                }
                if (tablet) {
                    Text(srcName, color = OttGoldDim, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x1A168BFF))
                            .dlClickable(RoundedCornerShape(8.dp), OttGold, 1.dp, 1f) { vm.cycleSource() }.padding(horizontal = 9.dp, vertical = 7.dp))
                }
            }
        }
    }
}

package com.dvhamham.manager.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.dvhamham.BuildConfig
import androidx.compose.ui.res.stringResource
import com.dvhamham.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController
) {
    Scaffold(
        topBar = { AboutTopAppBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            AboutContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutTopAppBar(navController: NavController) {
    TopAppBar(
        title = { Text(stringResource(R.string.about_title)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_description_back))
            }
        }
    )
}

@Composable
fun AboutContent() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppTitle()
        Spacer(modifier = Modifier.height(16.dp))
        AppDescription()
        Spacer(modifier = Modifier.height(32.dp))
        AppVersionSection()
        Spacer(modifier = Modifier.height(16.dp))
        AppDeveloperSection()
    }
}

@Composable
fun AppTitle() {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AppDescription() {
    Text(
        text = stringResource(R.string.app_description),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
fun AppVersionSection() {
    AppVersionTitle()
    Spacer(modifier = Modifier.height(16.dp))
    AppVersionValue()
}

@Composable
fun AppVersionTitle() {
    Text(
        text = stringResource(R.string.version),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.dp)
    )
}

@Composable
fun AppVersionValue() {
    Text(
        text = BuildConfig.VERSION_NAME,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun AppDeveloperSection() {
    AppDeveloperTitle()
    Spacer(modifier = Modifier.height(16.dp))
    AppDeveloperValue()
}

@Composable
fun AppDeveloperTitle() {
    Text(
        text = stringResource(R.string.developed_by),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun AppDeveloperValue() {
    val context = LocalContext.current
    Text(
        text = stringResource(R.string.developer_name),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(top = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/noobexon1"))
                context.startActivity(intent)
            }
    )
}
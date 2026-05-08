package alragar2.isi3.uv.flagflash.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import alragar2.isi3.uv.flagflash.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text

@Composable
fun MainScreen(
    onJugarClick: () -> Unit,
    onMultijugadorClick: () -> Unit,
    onGaleriaClick: () -> Unit,
    onRankingClick: () -> Unit,
    onOptionsClick: () -> Unit

) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(214.dp)
        )

        Button(
            onClick = onJugarClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF2E8DFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Jugar")
        }

        Button(
            onClick = onMultijugadorClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF2E8DFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Multijugador")
        }

        Button(
            onClick = onGaleriaClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF2E8DFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Galería")
        }

        Button(
            onClick = onRankingClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF2E8DFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Ranking")
        }

        Button(
            onClick = onOptionsClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF2E8DFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Opciones")
        }
    }
}

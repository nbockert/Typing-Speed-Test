package com.example.typingspeedtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import android.content.Context

import org.xmlpull.v1.XmlPullParser
import androidx.compose.ui.Alignment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
          TypingSpeedTestScreen()
        }
    }
}
fun loadWords(context:Context):List<String>{
    val words = mutableListOf<String>()
    val parser = context.resources.getXml(R.xml.typingwords)
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT){
        if(eventType == XmlPullParser.START_TAG && parser.name=="word"){
            val word = parser.getAttributeValue(null,"text") ?:""
            words.add(word)
        }
        eventType = parser.next()
    }
    return words
}

@Composable
fun TypingSpeedTestScreen() {
    val context = LocalContext.current
    var words by remember { mutableStateOf(loadWords(context).shuffled()) }
    //source: ChatGPT
    var currentWords by remember { mutableStateOf (words.take(3).toMutableList())}
    var textTyped by remember { mutableStateOf(TextFieldValue(""))}
    var correctCount by remember {  mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    var typedWords = remember { mutableStateOf(mutableSetOf<String>()) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var wpm by remember { mutableStateOf(0) }
    LaunchedEffect(Unit){
        while(true){
            delay(5000)


                // Source: ChatGPT
                currentWords = currentWords.map { word ->
                    if (word != textTyped.text.trim()) {
                        val availableWords = words.filterNot { currentWords.contains(it) || typedWords.value.contains(it) }
                        if (availableWords.isNotEmpty()) {
                            availableWords.random() // Replace with a new word
                        }else{
                            word
                        }
                    } else word // Keep words that are actively being typed
                }.toMutableList()



        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(100.dp),
        contentAlignment = Alignment.Center // This will center the content within the Box
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(100.dp)
    ) {
        Text(
            text = "Typing Speed Test",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(
            modifier = Modifier.height(18.dp)
        )
        LazyColumn(
            modifier = Modifier
                .weight(9f)
        ) {
            items(currentWords) { word ->
                Text(text = word, style = MaterialTheme.typography.bodyLarge)
            }
        }

        TextField(
            value = textTyped,
            onValueChange = { newText ->
                textTyped = newText
                if (newText.text.trim() in currentWords) {
                    coroutineScope.launch{
                        println("Before update - Current Words: $currentWords | Typed Text: ${textTyped.text}")
                        currentWords.remove(newText.text.trim())
                        typedWords.value.add(newText.text.trim())
                        println("After update - Current Words: $currentWords | Typed Words: ${typedWords.value}")
                        correctCount++
                        //source: chatGPT
                        val elapsedTimeInSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                        wpm = ((correctCount / elapsedTimeInSeconds) * 60).toInt()

                        textTyped = TextFieldValue("")
                        val untypedWords =
                            words.filterNot { currentWords.contains(it) || typedWords.value.contains(it) }
                        println("Untyped Words: $untypedWords")
                        if (untypedWords.isNotEmpty()) {
                            val newWord = untypedWords.random()
                            currentWords.add(newWord)
                        }

                    }


                }
            },
            placeholder = { Text("Type the words here...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Words Per Minute: $wpm",
            style = MaterialTheme.typography.bodyLarge
        )
    }

    }


}


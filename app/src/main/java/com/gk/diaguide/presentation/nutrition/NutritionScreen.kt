package com.gk.diaguide.presentation.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gk.diaguide.R
import com.gk.diaguide.core.ui.Dimens

private data class BreakfastRecipe(
    val title: String,
    val shortDescription: String,
    val steps: List<String>,
    val tags: List<String>,
    val source: String,
    val imageUrl: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen() {
    val recipes = demoBreakfastRecipes()
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nutrition_title)) }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing),
        ) {
            item {
                Text(
                    text = stringResource(R.string.nutrition_intro),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            items(recipes) { recipe ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEAF3FF),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AsyncImage(
                            model = recipe.imageUrl,
                            contentDescription = recipe.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .clip(MaterialTheme.shapes.medium),
                        )
                        Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                        Text(recipe.shortDescription, style = MaterialTheme.typography.bodyMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            recipe.tags.forEach { tag ->
                                AssistChip(onClick = {}, label = { Text(tag) }, enabled = false)
                            }
                        }
                        recipe.steps.forEachIndexed { index, step ->
                            Text("${index + 1}. $step", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            stringResource(R.string.nutrition_source, recipe.source),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun demoBreakfastRecipes(): List<BreakfastRecipe> = listOf(
    BreakfastRecipe(
        title = "Запечённая овсянка с ягодами",
        shortDescription = "Подходит для заготовки на несколько дней. Основа: овсянка, ягоды и несладкое растительное молоко.",
        steps = listOf(
            "Смешайте овсяные хлопья, корицу, разрыхлитель и щепотку соли.",
            "Отдельно пробейте молоко, банан, немного авокадо, яйцо и ваниль.",
            "Соедините смеси, добавьте ягоды и выпекайте около 35-40 минут при 190°C.",
        ),
        tags = listOf("#утро", "#демо", "#клетчатка", "#условный_тег_для_рекомендаций"),
        source = "American Heart Association: Berry Avocado Baked Oats",
        imageUrl = "https://images.unsplash.com/photo-1517673400267-0251440c45dc?auto=format&fit=crop&w=1200&q=80",
    ),
    BreakfastRecipe(
        title = "Авокадо-тост с яйцом",
        shortDescription = "Быстрый вариант на 5 минут: цельнозерновой тост, авокадо и яйцо.",
        steps = listOf(
            "Подсушите ломтик цельнозернового хлеба.",
            "Разомните авокадо с перцем и чесночным порошком.",
            "Выложите на тост и добавьте жареное яйцо сверху.",
        ),
        tags = listOf("#утро", "#быстро", "#белок", "#условный_тег_для_рекомендаций"),
        source = "EatingWell: Avocado-Egg Toast",
        imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=1200&q=80",
    ),
    BreakfastRecipe(
        title = "Греческий йогурт с клубникой и овсянкой",
        shortDescription = "Слой йогурта, ягоды и овсяные хлопья; можно сделать вечером и оставить в холодильнике.",
        steps = listOf(
            "Смешайте натуральный греческий йогурт с небольшим количеством ванили.",
            "В стакан выложите слоями йогурт, нарезанную клубнику и овсяные хлопья.",
            "Подавайте сразу или охладите 20-30 минут для более мягкой текстуры.",
        ),
        tags = listOf("#утро", "#перекус", "#низкая_соль", "#условный_тег_для_рекомендаций"),
        source = "Heart Research UK: Greek Yogurt & Strawberry Parfaits",
        imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=1200&q=80",
    ),
)

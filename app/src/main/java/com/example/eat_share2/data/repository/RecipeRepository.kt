package com.example.eat_share2.data.repository

import com.example.eat_share2.data.models.Category
import com.example.eat_share2.data.models.Recipe
import kotlinx.coroutines.delay

class RecipeRepository {
    
    // Mock data for demonstration
    private val categories = listOf(
        Category("1", "Breakfast", "🍳"),
        Category("2", "Lunch", "🥗"),
        Category("3", "Dinner", "🍽️"),
        Category("4", "Dessert", "🍰"),
        Category("5", "Snacks", "🍿"),
        Category("6", "Beverages", "🥤")
    )
    
    private val recipes = listOf(
        Recipe(
            id = "1",
            title = "Spaghetti Carbonara",
            description = "Classic Italian pasta dish with eggs, cheese, and pancetta",
            imageUrl = "https://images.pexels.com/photos/4518843/pexels-photo-4518843.jpeg",
            prepTime = "30 min",
            rating = 4.5f,
            reviewCount = 278,
            category = "Dinner",
            ingredients = listOf("Spaghetti", "Eggs", "Pancetta", "Parmesan", "Black pepper"),
            instructions = listOf(
                "Cook spaghetti according to package directions",
                "Fry pancetta until crispy",
                "Mix eggs with cheese",
                "Combine hot pasta with egg mixture",
                "Add pancetta and serve immediately"
            )
        ),
        Recipe(
            id = "2",
            title = "Chicken Caesar Salad",
            description = "Fresh romaine lettuce with grilled chicken and caesar dressing",
            imageUrl = "https://images.pexels.com/photos/2097090/pexels-photo-2097090.jpeg",
            prepTime = "20 min",
            rating = 4.7f,
            reviewCount = 189,
            category = "Lunch",
            ingredients = listOf("Romaine lettuce", "Chicken breast", "Croutons", "Parmesan", "Caesar dressing"),
            instructions = listOf(
                "Grill chicken breast until cooked through",
                "Wash and chop romaine lettuce",
                "Make caesar dressing",
                "Combine all ingredients",
                "Serve immediately"
            )
        ),
        Recipe(
            id = "3",
            title = "Pancakes with Berries",
            description = "Fluffy pancakes topped with fresh berries and maple syrup",
            imageUrl = "https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg",
            prepTime = "25 min",
            rating = 4.8f,
            reviewCount = 342,
            category = "Breakfast",
            ingredients = listOf("Flour", "Eggs", "Milk", "Berries", "Maple syrup"),
            instructions = listOf(
                "Mix dry ingredients",
                "Combine wet ingredients",
                "Mix batter until just combined",
                "Cook pancakes on griddle",
                "Serve with berries and syrup"
            )
        ),
        Recipe(
            id = "4",
            title = "Chocolate Chip Cookies",
            description = "Classic homemade chocolate chip cookies",
            imageUrl = "https://images.pexels.com/photos/230325/pexels-photo-230325.jpeg",
            prepTime = "45 min",
            rating = 4.6f,
            reviewCount = 156,
            category = "Dessert",
            ingredients = listOf("Flour", "Butter", "Sugar", "Chocolate chips", "Vanilla"),
            instructions = listOf(
                "Cream butter and sugar",
                "Add eggs and vanilla",
                "Mix in flour",
                "Fold in chocolate chips",
                "Bake for 10-12 minutes"
            )
        ),
        Recipe(
            id = "5",
            title = "Grilled Salmon",
            description = "Perfectly grilled salmon with herbs and lemon",
            imageUrl = "https://images.pexels.com/photos/725991/pexels-photo-725991.jpeg",
            prepTime = "35 min",
            rating = 4.4f,
            reviewCount = 203,
            category = "Dinner",
            ingredients = listOf("Salmon fillet", "Lemon", "Herbs", "Olive oil", "Salt"),
            instructions = listOf(
                "Preheat grill to medium-high",
                "Season salmon with herbs and salt",
                "Brush with olive oil",
                "Grill for 4-5 minutes per side",
                "Serve with lemon wedges"
            )
        ),
        Recipe(
            id = "6",
            title = "Avocado Toast",
            description = "Simple and healthy avocado toast with toppings",
            imageUrl = "https://images.pexels.com/photos/1351238/pexels-photo-1351238.jpeg",
            prepTime = "10 min",
            rating = 4.3f,
            reviewCount = 89,
            category = "Breakfast",
            ingredients = listOf("Bread", "Avocado", "Salt", "Pepper", "Lime"),
            instructions = listOf(
                "Toast bread until golden",
                "Mash avocado with lime",
                "Season with salt and pepper",
                "Spread on toast",
                "Add desired toppings"
            )
        )
    )
    
    suspend fun getCategories(): List<Category> {
        delay(500) // Simulate network delay
        return categories
    }
    
    suspend fun getPopularRecipes(): List<Recipe> {
        delay(500) // Simulate network delay
        return recipes.sortedByDescending { it.rating }
    }
    
    suspend fun searchRecipes(query: String): List<Recipe> {
        delay(500) // Simulate network delay
        return if (query.isBlank()) {
            recipes
        } else {
            recipes.filter { 
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.ingredients.any { ingredient -> ingredient.contains(query, ignoreCase = true) }
            }
        }
    }
    
    suspend fun getRecipesByCategory(category: String): List<Recipe> {
        delay(500) // Simulate network delay
        return recipes.filter { it.category.equals(category, ignoreCase = true) }
    }
    
    suspend fun getRecipeById(id: String): Recipe? {
        delay(500) // Simulate network delay
        return recipes.find { it.id == id }
    }
}
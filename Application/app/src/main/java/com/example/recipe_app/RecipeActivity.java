package com.example.recipe_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RecipeActivity extends AppCompatActivity implements NetworkingService.NetworkingListener {
    NetworkingService networkingManager;
    JsonService jsonService;
    String recipeName;
    TextView recipeNameText;
    TextView cuisineTypeText;
    TextView mealTypeText;
    TextView totalTimeText;
    TextView ingredientsText;
    TextView caloriesText;
    ImageView imageView;

    Recipe newRecipe = new Recipe();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        //get access to the Views
        recipeNameText = findViewById(R.id.recipeName);
        cuisineTypeText = findViewById(R.id.cuisineType);
        mealTypeText = findViewById(R.id.mealType);
        totalTimeText = findViewById(R.id.totalTime);
        ingredientsText = findViewById(R.id.ingredients);
        caloriesText = findViewById(R.id.calories);
        imageView = findViewById(R.id.recipeImage);

        //get the data sent from Main Activity
        //gets the name of the recipe selected
        recipeName = getIntent().getStringExtra("recipeName");
        recipeNameText.setText(recipeName);

        networkingManager = ((myApp) getApplication()).getNetworkingService();
        jsonService = ((myApp) getApplication()).getJsonService();
        networkingManager.listener = this;

        //get the recipe info for the recipe selected
        networkingManager.getRecipeData(recipeName);
    }

    //listener for when recipe info is returned from background thread
    @Override
    public void dataListener(String jsonRecipeString) {
        Recipe data = jsonService.getRecipeData(recipeName, jsonRecipeString);

        cuisineTypeText.setText(data.getCuisineType());
        mealTypeText.setText(data.getMealType());
        totalTimeText.setText(data.getTime());
        ingredientsText.setText(data.getIngredients());
        caloriesText.setText(data.getCalories());

        //get the image using background thread
        networkingManager.getImageData(data.getImage());
    }

    //listener for when image is returned from background thread
    @Override
    public void imageListener(Bitmap image) {
        imageView.setImageBitmap(image);
    }

    //when saved button is clicked
    public void saveRecipeToDatabase(View view) {
        newRecipe.setNewRecipe(recipeNameText.getText().toString(), ingredientsText.getText().toString(), caloriesText.getText().toString(), totalTimeText.getText().toString(), cuisineTypeText.getText().toString(), mealTypeText.getText().toString());
        DatabaseManager.insertRecipeIntoDatabase(newRecipe);
        Toast.makeText(this, getString(R.string.recipe_recipe_saved_to_database), Toast.LENGTH_LONG).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.report_menu_item:{
                Intent toFavouriteRecipesActivity = new Intent(this, FavouritesActivity.class);
                startActivity(toFavouriteRecipesActivity);
                break;
            }
            default:{
                Intent toMainActivity = new Intent(this, MainActivity.class);
                startActivity(toMainActivity);
                break;
            }
        }
        return true;
    }
}
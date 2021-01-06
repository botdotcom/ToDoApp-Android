package com.example.todoapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;
    List<String> items;

    // widget objects
    Button addButton;
    EditText editTextItem;
    RecyclerView rvItemsList;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // widgets
        addButton = findViewById(R.id.addButton);
        editTextItem = findViewById(R.id.todoText);
        rvItemsList = findViewById(R.id.rvItems);

        // mock data
        /*items = new ArrayList<>();
        items.add("Buy milk");
        items.add("Go to gym");
        items.add("Pay rent");*/

        loadItemsFromFile();

        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                items.remove(position);
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
                saveItemsToFile();
            }
        };

        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(KEY_ITEM_TEXT, items.get(position));
                intent.putExtra(KEY_ITEM_POSITION, position);
                startActivityForResult(intent, EDIT_TEXT_CODE);
            }
        };

        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        rvItemsList.setAdapter(itemsAdapter);
        rvItemsList.setLayoutManager(new LinearLayoutManager(this));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String todoItem = editTextItem.getText().toString();
                items.add(todoItem);
                itemsAdapter.notifyItemInserted(items.size() - 1);
                editTextItem.setText("");
                Toast.makeText(getApplicationContext(), "Item was added", Toast.LENGTH_SHORT).show();
                saveItemsToFile();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            items.set(position, itemText);
            itemsAdapter.notifyItemChanged(position);
            saveItemsToFile();
            Toast.makeText(getApplicationContext(), "Item was updated", Toast.LENGTH_SHORT).show();
        }
        else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile() {
        return new File(getFilesDir(), "data.txt");
    }

    private void loadItemsFromFile() {
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading items from file", e);
            items = new ArrayList<>();
        }
    }

    private void saveItemsToFile() {
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing items to file", e);
        }
    }
}
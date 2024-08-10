package com.example.domino;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;

public class JoueursActivity extends AppCompatActivity {

    private LinearLayout inputContainer;
    private int inputCount = 0;
    private PlayerDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joueurs);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inputContainer = findViewById(R.id.inputContainer);
        FloatingActionButton addPlayerButton = findViewById(R.id.add);

        databaseHelper = new PlayerDatabaseHelper(this);

        addPlayerButton.setOnClickListener(view -> showAddPlayerDialog());
        loadPlayersFromDatabase();
    }


    private void showAddPlayerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_player);

        TextInputEditText playerNameInput = dialog.findViewById(R.id.playerNameInput);
        dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            String playerName = playerNameInput.getText().toString().trim();
            if (!TextUtils.isEmpty(playerName)) {
                if (playerName.length() > 15) {
                    playerNameInput.setError("Le nom du joueur ne peut pas dépasser 15 caractères");
                } else {
                    addNewPlayer(playerName);
                    dialog.dismiss();
                }
            } else {
                playerNameInput.setError("Le nom du joueur est obligatoire");
            }
        });

        dialog.show();
    }

    private void showEditPlayerDialog(Player player) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_player);

        TextInputEditText playerNameInput = dialog.findViewById(R.id.playerNameInput);
        playerNameInput.setText(player.getName());

        dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            String newName = playerNameInput.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                if (newName.length() > 15) {
                    playerNameInput.setError("Le nom du joueur ne peut pas dépasser 15 caractères");
                } else {
                    player.setName(newName);
                    databaseHelper.updatePlayer(player);
                    updateUI();
                    dialog.dismiss();
                }
            } else {
                playerNameInput.setError("Le nom du joueur est obligatoire");
            }
        });

        dialog.show();
    }


    private void showDeletePlayerDialog(Player player) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le joueur")
                .setMessage("Voulez-vous vraiment supprimer ce joueur ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    databaseHelper.deletePlayer(player.getId());
                    updateUI();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void addNewPlayer(String playerName) {
        Player newPlayer = new Player(playerName);
        long id = databaseHelper.addPlayer(newPlayer);
        newPlayer.setId((int) id); // Set the ID from the database
        inputCount++;
        updateUI();
    }

    private void loadPlayersFromDatabase() {
        List<Player> players = databaseHelper.getAllPlayers();
        for (Player player : players) {
            addPlayerView(player);
        }
    }

    private void updateUI() {
        inputContainer.removeAllViews();
        loadPlayersFromDatabase();
    }

    private void addPlayerView(Player player) {
        TextView playerView = new TextView(this);
        playerView.setText(player.getId() + " - " + player.getName());
        playerView.setTextSize(18);
        playerView.setTextColor(getResources().getColor(R.color.white));
        playerView.setPadding(16, 16, 16, 16);

        playerView.setOnClickListener(v -> showEditPlayerDialog(player));
        playerView.setOnLongClickListener(v -> {
            showDeletePlayerDialog(player);
            return true;
        });

        inputContainer.addView(playerView);
    }
}

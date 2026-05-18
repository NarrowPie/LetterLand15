package com.example.letterland;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfilesActivity extends AppCompatActivity {

    private RecyclerView rvProfiles;
    private ProfileAdapter adapter;
    private SharedPreferences prefs;
    private String profileAwaitingImage = ""; // Remembers who we are picking a picture for!

    // 📸 THE IMAGE PICKER
    private final ActivityResultLauncher<String> pickAvatarLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && !profileAwaitingImage.isEmpty()) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        saveAvatarToStorage(profileAwaitingImage, bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        prefs = getSharedPreferences("LetterLandMemory", MODE_PRIVATE);
        rvProfiles = findViewById(R.id.rvProfiles);
        rvProfiles.setLayoutManager(new LinearLayoutManager(this));

        ExtendedFloatingActionButton fabAddProfile = findViewById(R.id.fabAddProfile);

        // 🔙 SET UP THE BACK BUTTON & BEHAVIOR
        ImageButton btnBack = findViewById(R.id.btnBackProfiles);

        // 🚀 CHECK THE SECRET MESSAGE FROM MAIN ACTIVITY
        boolean isMandatoryLogin = getIntent().getBooleanExtra("IS_MANDATORY_LOGIN", false);

        if (isMandatoryLogin) {
            // It's a startup login: Hide the in-app back button completely!
            btnBack.setVisibility(View.GONE);
        } else {
            // Opened from the menu: Show it and let it close the screen normally
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                finish();
            });
        }

        // 🚀 HANDLE THE PHYSICAL PHONE BACK BUTTON
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isMandatoryLogin) {
                    // They tried to back out of the mandatory login -> Quit the entire app!
                    finishAffinity();
                } else {
                    // They are just changing profiles -> Return to the main menu normally
                    finish();
                }
            }
        });

        loadProfiles();

        fabAddProfile.setOnClickListener(v -> {
            SoundManager.getInstance(ProfilesActivity.this).playClick(); // Pop sound!
            showAddProfileDialog();
        });
    }

    private void loadProfiles() {
        Set<String> savedProfiles = prefs.getStringSet("ALL_PROFILES", new HashSet<>());
        List<String> profileList = new ArrayList<>(savedProfiles);
        adapter = new ProfileAdapter(profileList);
        rvProfiles.setAdapter(adapter);
    }

    // 💾 SAVES THE CHOSEN PICTURE TO THE PHONE
    private void saveAvatarToStorage(String profileName, Bitmap bitmap) {
        String fileName = "avatar_" + profileName + ".jpg";
        java.io.File file = new java.io.File(getExternalFilesDir(null), fileName);

        try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            // Save the file path to memory so the app remembers it!
            prefs.edit().putString("AVATAR_" + profileName, file.getAbsolutePath()).apply();

            runOnUiThread(() -> {
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                loadProfiles(); // Refresh the list to show the new picture
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddProfileDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter your Name");

        new AlertDialog.Builder(this)
                .setTitle("New Player")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newName = input.getText().toString().trim().toUpperCase();

                    if (!newName.isEmpty()) {
                        Set<String> oldProfiles = prefs.getStringSet("ALL_PROFILES", new HashSet<>());
                        Set<String> newProfiles = new HashSet<>(oldProfiles);
                        newProfiles.add(newName);

                        prefs.edit().putStringSet("ALL_PROFILES", newProfiles).apply();
                        loadProfiles();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================
    // THE LIST ADAPTER
    // ==========================================
    private class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
        private final List<String> profiles;

        public ProfileAdapter(List<String> profiles) {
            this.profiles = profiles;
        }

        @NonNull
        @Override
        public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
            return new ProfileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
            String profileName = profiles.get(position);
            holder.tvProfileName.setText(profileName);

            // 🖼️ LOAD THE PROFILE PICTURE (Or show default if they don't have one)
            String avatarPath = prefs.getString("AVATAR_" + profileName, null);
            if (avatarPath != null) {
                holder.ivAvatar.setImageURI(android.net.Uri.parse(avatarPath));
            } else {
                holder.ivAvatar.setImageResource(R.drawable.admin_pic);
                // Your default logo
            }

            // ⭐ HIGHLIGHT THE ACTIVE PLAYER!
            String activePlayer = prefs.getString("ACTIVE_PROFILE", "");
            com.google.android.material.card.MaterialCardView cardView = (com.google.android.material.card.MaterialCardView) holder.itemView;

            if (profileName.equals(activePlayer)) {
                // If this is the playing kid, make their card bold green!
                cardView.setStrokeColor(android.graphics.Color.parseColor("#4CAF50"));
                cardView.setStrokeWidth(8);
            } else {
                // Otherwise, normal blue border
                cardView.setStrokeColor(android.graphics.Color.parseColor("#29B6F6"));
                cardView.setStrokeWidth(3);
            }

            // 👆 TAP THE PICTURE TO CHANGE IT
            holder.ivAvatar.setOnClickListener(v -> {
                SoundManager.getInstance(ProfilesActivity.this).playClick();
                profileAwaitingImage = profileName;
                pickAvatarLauncher.launch("image/*");
            });

            // 1. SELECT A PROFILE TO PLAY
            holder.itemView.setOnClickListener(v -> {
                SoundManager.getInstance(ProfilesActivity.this).playClick(); // Pop sound!

                // Save them as the active player
                prefs.edit().putString("ACTIVE_PROFILE", profileName).apply();
                Toast.makeText(ProfilesActivity.this, profileName + " is now playing!", Toast.LENGTH_SHORT).show();

                // Automatically close this screen and return to MainActivity
                finish();
            });

            // --- FIXED DELETE A PROFILE LOGIC ---
            holder.btnDeleteProfile.setOnClickListener(v -> {
                SoundManager.getInstance(ProfilesActivity.this).playClick(); // Pop sound!
                new AlertDialog.Builder(ProfilesActivity.this)
                        .setTitle("Delete " + profileName + "?")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            Set<String> oldProfiles = prefs.getStringSet("ALL_PROFILES", new HashSet<>());
                            Set<String> newProfiles = new HashSet<>(oldProfiles);
                            newProfiles.remove(profileName);
                            prefs.edit().putStringSet("ALL_PROFILES", newProfiles).apply();
                            prefs.edit().remove("AVATAR_" + profileName).apply(); // Delete picture link

                            // BUG FIX: Switch to another profile if the active one was deleted
                            if (profileName.equals(prefs.getString("ACTIVE_PROFILE", ""))) {
                                if (!newProfiles.isEmpty()) {
                                    // Grab any available profile (e.g., "Sweet") and make it active
                                    String fallbackProfile = newProfiles.iterator().next();
                                    prefs.edit().putString("ACTIVE_PROFILE", fallbackProfile).apply();
                                } else {
                                    // No profiles left, set to empty
                                    prefs.edit().putString("ACTIVE_PROFILE", "").apply();
                                }
                            }
                            loadProfiles();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return profiles.size();
        }

        class ProfileViewHolder extends RecyclerView.ViewHolder {
            TextView tvProfileName;
            ImageView ivAvatar;
            ImageButton btnDeleteProfile;

            public ProfileViewHolder(@NonNull View itemView) {
                super(itemView);
                tvProfileName = itemView.findViewById(R.id.tvProfileName);
                ivAvatar = itemView.findViewById(R.id.ivAvatar);
                btnDeleteProfile = itemView.findViewById(R.id.btnDeleteProfile);
            }
        }
    }
}
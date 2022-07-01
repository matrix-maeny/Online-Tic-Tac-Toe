package com.matrix_maeny.onlinetictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.onlinetictactoe.databinding.ActivityMainBinding;
import com.matrix_maeny.onlinetictactoe.registerActivities.LoginActivity;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ResultDialog.ResultDialogListener {

    private ActivityMainBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    private String connectUsername = null;
    private boolean alreadySetuped = false;
    private boolean mainEntry = false;
    private boolean gameOver = false;
    private boolean winStatus = false;
    private boolean isRestartGame = false;
    public static boolean host = false;
    private boolean singleCall = true;
    private String connectedUserUid = null;
    private String currentUserUid = null;
    String gameSpaceKey = null;
    private volatile Moves moves = new Moves();

    private String clientUsername = null, hostUsername = null, currentUsername = null;
    private MediaPlayer mediaPlayer = null;
    private boolean sounds = true;
    private boolean isOnline = true;


    private ImageView[] boxes;
    private ResultDialog dialog = new ResultDialog();


    private ArrayList<Integer> player1Moves = new ArrayList<>();
    private ArrayList<Integer> player2Moves = new ArrayList<>();

    private final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());


        initialize();

        setUpClientValueListeners();
    }


    private void initialize() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserUid = Objects.requireNonNull(auth.getCurrentUser()).getUid();


        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Fetching data...");
        progressDialog.show();

        getCurrentUserData();
        setUserStatus(true);
        getOnlineStatus();

        boxes = new ImageView[]{binding.move1Iv, binding.move2Iv, binding.move3Iv, binding.move4Iv, binding.move5Iv, binding.move6Iv,
                binding.move7Iv, binding.move8Iv, binding.move9Iv};

        binding.mainConnectBtn.setOnClickListener(connectBtnListener);

        binding.moveLayout1.setOnClickListener(box1Listener);
        binding.moveLayout2.setOnClickListener(box2Listener);
        binding.moveLayout3.setOnClickListener(box3Listener);
        binding.moveLayout4.setOnClickListener(box4Listener);
        binding.moveLayout5.setOnClickListener(box5Listener);
        binding.moveLayout6.setOnClickListener(box6Listener);
        binding.moveLayout7.setOnClickListener(box7Listener);
        binding.moveLayout8.setOnClickListener(box8Listener);
        binding.moveLayout9.setOnClickListener(box9Listener);

        for (int i = 0; i < 9; i++) {
            player1Moves.add(-1);
            player2Moves.add(-1);
        }
        setLocalMovesToNeg1();


    }

    private void getOnlineStatus() {
        firebaseDatabase.getReference().child("Users").child(currentUserUid)
                .child("statusOnline").addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isOnline = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
//                        Toast.makeText(MainActivity.this, "" + isOnline, Toast.LENGTH_SHORT).show();
//                        if (isOnline) {
//                            if (host) {
//                                binding.statusOnlineTv.setText(clientUsername + " : Online");
//                            } else
//                                binding.statusOnlineTv.setText(hostUsername + " : Online");
//                        } else {
//                            if (host) {
//                                binding.statusOnlineTv.setText(clientUsername + " : Offline");
//                            } else
//                                binding.statusOnlineTv.setText(hostUsername + " : Offline");
//
//                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getClientOnlineStatus() {
        firebaseDatabase.getReference().child("Users").child(connectedUserUid)
                .child("statusOnline").addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isOnline = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
//                        Toast.makeText(MainActivity.this, "" + isOnline, Toast.LENGTH_SHORT).show();

                        if (isOnline) {
                            if (host) {
                                binding.statusOnlineTv.setText(Html.fromHtml(clientUsername + " : <b>Online</b>"));
                            } else
                                binding.statusOnlineTv.setText(Html.fromHtml(hostUsername + " : <b>Online</b>"));
                        } else {
                            if (host) {
                                binding.statusOnlineTv.setText(Html.fromHtml(clientUsername + " : <b>Offline</b>"));
                            } else
                                binding.statusOnlineTv.setText(Html.fromHtml(hostUsername + " : <b>Offline</b>"));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setLocalMovesToNeg1() {
        for (int i = 0; i < 9; i++) {
            player1Moves.set(i, -1);
            player2Moves.set(i, -1);
        }
    }

    private void setUserStatus(boolean online) {
        firebaseDatabase.getReference().child("Users").child(currentUserUid)
                .child("statusOnline").setValue(online);
    }

    private void getCurrentUserData() {
        firebaseDatabase.getReference().child("Users").child(currentUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
                            currentUsername = model.getUsername();
                            Objects.requireNonNull(getSupportActionBar()).setTitle(currentUsername);
                        }
                        mainEntry = true;
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    View.OnClickListener connectBtnListener = v -> {

//        playClick();
        if (binding.mainConnectBtn.getText().equals(getString(R.string.disconnect))) {
            gameOver = true;
            destroyConnection();
            return;
        }

        if (mainEntry && checkUsername()) {
            gameOver = false;
            if (!connectUsername.equals(currentUsername)) {
                host = true;
                establishConnection();
            } else {
                Toast.makeText(this, "Can't connect to yourself", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Please wait fetching data...", Toast.LENGTH_SHORT).show();
        }
    };


    private void setUpClientValueListeners() {

        DatabaseReference reference = firebaseDatabase.getReference().child("Users").child(currentUserUid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!host) {
                    clientUsername = currentUsername;
                    UserModel model = snapshot.getValue(UserModel.class);
                    if (model != null && !model.getConnectedTo().equals("")) {
                        connectedUserUid = model.getConnectedTo();
//                        clientUsername = model.getUsername();
                        if (!alreadySetuped) {
                            setupListenerForHost();
                        }

                        if (singleCall) {
                            firebaseDatabase.getReference().child("Users").child(connectedUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    UserModel model1 = snapshot.getValue(UserModel.class);
                                    if (model1 != null) {
                                        hostUsername = model1.getUsername();


                                        //                                    binding.mainTurnTv.setText(hostUsername);
                                        moves.setFirstMove(hostUsername);
                                        moves.setTurn(hostUsername);
                                        moves.setSecondMove(clientUsername);
                                        Log.i("establish1", "in-1");
                                        enterIntoGameSpace();
                                        singleCall = false;
                                        setUpUiForResult(model1.getUsername(), true);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }


                        if (!singleCall) {
                            enterIntoGameSpace();
                            Log.i("establish1", "in-2");

                        }
                    } else {
                        if (connectedUserUid != null && gameSpaceKey != null) {
                            destroyConnection();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void establishConnection() {

        // here i'm setting both users ids in both sides... so client id is seated in the current user data, and current user id
        // is seated in the client data;

        DatabaseReference reference = firebaseDatabase.getReference().child("Users");
        startResultLoaderDialog("Searching User...", "Wait few seconds...");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                long j = snapshot.getChildrenCount();
                for (DataSnapshot s : snapshot.getChildren()) {
                    i++;
                    UserModel model = s.getValue(UserModel.class);
                    if (model != null) {
                        if (connectUsername.equals(model.getUsername())) {

                            if (model.isStatusOnline()) {
                                Log.i("establish", "Entered in connection");
                                connectedUserUid = s.getKey();
                                clientUsername = model.getUsername();
                                hostUsername = currentUsername;
                                if (!alreadySetuped) {
                                    setupListenerForHost();
                                }
                                model.setConnectedTo(currentUserUid); // setting value in client

                                // setting value to client data
                                reference.child(connectedUserUid).setValue(model).addOnCompleteListener(task -> {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "connected to : " + model.getUsername(), Toast.LENGTH_SHORT).show();

                                        // setting value to current data;
                                        reference.child(currentUserUid).child("connectedTo").setValue(connectedUserUid);

                                        setUpUiForResult(model.getUsername(), true);

//                                        moves.setTurn(hostUsername);
//                                        moves.setFirstMove(hostUsername);
                                        enterIntoGameSpace();


                                    } else {
                                        Toast.makeText(MainActivity.this, "connection failed", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();

                                });

                            } else {
                                Toast.makeText(MainActivity.this, "User not active", Toast.LENGTH_SHORT).show();
                            }


                            progressDialog.dismiss();
                            break;

                        } else if (i == j) {
                            Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void setupListenerForHost() {
        String mainId = null;
        if (host) {
            mainId = currentUserUid;
        } else {
            mainId = connectedUserUid;
        }
        firebaseDatabase.getReference().child("Users").child(mainId).child("connectedTo")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        if (value != null && value.equals("") && gameSpaceKey != null) {
                            destroyConnection();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        alreadySetuped = true;
    }

    @SuppressLint("SetTextI18n")
    private void destroyConnection() {
        if (gameSpaceKey == null) {
            return;
        }
        resetGame();


        binding.mainTurnTv.setText("Turn appears here");

        firebaseDatabase.getReference().child("Users").child(currentUserUid).child("connectedTo").setValue("")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseDatabase.getReference().child("Users").child(connectedUserUid).child("connectedTo").setValue("");
                        setUpUiForResult(null, false);
                        gameSpaceKey = null;
                        host = false;
                        singleCall = true;

                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void resetGame() {
        Moves moves = new Moves();
        this.moves = moves;
        setAllValuesToInitialStatus();
        firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey)
                .setValue(moves).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
//                        gameSpaceKey = null;
                        hideAll();
                        try {
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void playAgain() {
        for (int i = 0; i < 9; i++) {
            moves.setGameMoves(i, -1);
        }
        moves.setTouchCount(-1);
        moves.setWinner("DRAW");
        setAllValuesToInitialStatus();
        firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey)
                .setValue(moves).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
//                        gameSpaceKey = null;
                        hideAll();
                    }
                });
    }

    private void setAllValuesToInitialStatus() {
        gameOver = false;
        winStatus = false;
        setLocalMovesToNeg1();
    }

    private void hideAll() {
        binding.move1Iv.setVisibility(View.INVISIBLE);
        binding.move2Iv.setVisibility(View.INVISIBLE);
        binding.move3Iv.setVisibility(View.INVISIBLE);
        binding.move4Iv.setVisibility(View.INVISIBLE);
        binding.move5Iv.setVisibility(View.INVISIBLE);
        binding.move6Iv.setVisibility(View.INVISIBLE);
        binding.move7Iv.setVisibility(View.INVISIBLE);
        binding.move8Iv.setVisibility(View.INVISIBLE);
        binding.move9Iv.setVisibility(View.INVISIBLE);
    }


    private void setUpUiForResult(String name, boolean shouldConnect) {
        if (shouldConnect) {
            binding.textInputLayout2.setVisibility(View.GONE);
            binding.mainConnectionStatusTv.setText(String.format("Connected to %s", name));
            binding.mainConnectionStatusTv.setVisibility(View.VISIBLE);
            binding.mainConnectBtn.setText(getString(R.string.disconnect));
            binding.statusOnlineTv.setVisibility(View.VISIBLE);
        } else {
            binding.textInputLayout2.setVisibility(View.VISIBLE);
//            binding.mainConnectionStatusTv.setText(String.format("Connected to %s", name));
            binding.mainConnectionStatusTv.setVisibility(View.GONE);
            binding.mainConnectBtn.setText(getString(R.string.connect));
            binding.mainUsername.setText("");
            binding.statusOnlineTv.setVisibility(View.INVISIBLE);
        }
    }

    private void startResultLoaderDialog(String title, String message) {
        progressDialog.setMessage(message);
        progressDialog.setTitle(title);
        progressDialog.show();
    }

    private void setupGameSpaceKey() {
        Log.i("users", "host : " + hostUsername + "client: " + clientUsername);
        if (host) {
            Log.i("host", "yes : current id : " + currentUserUid + "client: " + connectedUserUid);
            gameSpaceKey = currentUserUid + connectedUserUid;
        } else {
            Log.i("host", "no : current id : " + currentUserUid + "client: " + connectedUserUid);
            gameSpaceKey = connectedUserUid + currentUserUid;
        }
    }

    private void enterIntoGameSpace() {

//        if (host) {
//            Log.i("host", "yes : current id : " + currentUserUid + "client: " + connectedUserUid);
//            gameSpaceKey = currentUserUid + connectedUserUid;
//        } else {
//            Log.i("host", "no : current id : " + currentUserUid + "client: " + connectedUserUid);
//            gameSpaceKey = connectedUserUid + currentUserUid;
//        }
        setupGameSpaceKey();

//        Log.i("values","Turn: "+moves.getTurn()+"\nFirst move"+moves.getFirstMove());

        firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey).setValue(moves);
        DatabaseReference reference = firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("establish", "Entered");

                // for online status
                getClientOnlineStatus();

                moves = snapshot.getValue(Moves.class);
                if (moves.getTurn().equals("") || moves.getFirstMove().equals("")) {
                    binding.mainTurnTv.setText("Error: try to reconnect");
                    return;
                }

                if (moves != null) {

                    if (isRestartGame) { // this boolean for accessing restart in both devices
                        playAgain(); // resetting every thing
                        try {
                            dialog.dismiss(); // dismissing the existed result dialog
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isRestartGame = false;
                    }
                    if (!winStatus && !gameOver) { // to update the board/ to continue the game, they shouldn't win and the
                        // game shouldn't complete
                        updateGameBoard();
                    }

                    if (!winStatus && checkWinStatus()) { // the boolean is for single access into the if statement
                        // a person won the game

                        startResultLoaderDialog("Loading Winner...", "wait few seconds..."); // starting animation for result

                        handler.postDelayed(() -> {                 // updating winner / uploading winner takes time... so
                            // we are using handler for 1.5 seconds
                            binding.mainTurnTv.setText("Winner: " + moves.getWinner());
                            try {
                                progressDialog.dismiss(); // you've to show it first, if not you'll get illegal state exception
                                showResultDialog();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            isRestartGame = true;
                        }, 1000);

                        winStatus = true;
                        gameOver = true;
                    } else {
                        if (!gameOver && checkGameOverStatus()) { // here game over is also used for the single access
                            // the game is completed without winning

                            startResultLoaderDialog("Loading...", "wait few seconds..."); // starting animation for result

                            handler.postDelayed(() -> {                 // updating winner / uploading winner takes time... so
                                // we are using handler for 1.5 seconds
                                binding.mainTurnTv.setText(moves.getWinner());
                                try {
                                    progressDialog.dismiss(); // you've to show it first, if not you'll get illegal state exception
                                    showResultDialog();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                isRestartGame = true;
                            }, 1000);
                            gameOver = true;
                        }
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateGameBoard() {
//        boolean circle = host;
//        boolean secondCircle = true;
//        int[] firstIndices = {-1, -1, -1, -1, -1};
//        int[] secondIndices = {-1, -1, -1, -1, -1};

        Log.i("establishGame", "establish game entered");
        int i, j = 1;

        for (i = 0; i < 9; i += 2, j += 2) {
            if (moves.getGameMoves().get(i) != -1) {
                boxes[moves.getGameMoves().get(i)].setImageResource(R.drawable.player_1);
                boxes[moves.getGameMoves().get(i)].setVisibility(View.VISIBLE);
                player1Moves.set(i, moves.getGameMoves().get(i));

            }
//            firstIndices[i] = moves.getGameMoves().get(i);
            if (j < 9 && moves.getGameMoves().get(j) != -1) {

                boxes[moves.getGameMoves().get(j)].setImageResource(R.drawable.player_2);
                boxes[moves.getGameMoves().get(j)].setVisibility(View.VISIBLE);
                player2Moves.set(j, moves.getGameMoves().get(j));

//                secondIndices[j] = moves.getGameMoves().get(j);
            }
        }

        setTurn();

//        if (firstMover.equals(currentUserUid)) {
//
//
//
//        } else if (firstMover.equals(clientUsername)) {
//
//        }
    }

    private void showResultDialog() {
//        dialogShowed = true;
        if (moves.getWinner().equals(currentUsername)) {
            ResultDialog.win = "WON";
            playWin();
        } else {
            ResultDialog.win = "LOSE";
            playLost();
        }

        if (moves.getWinner().equals("DRAW")) {
            ResultDialog.win = "DRAW";
            playDraw();
        }
        dialog.show(getSupportFragmentManager(), "Result dialog");
    }

    private void updateWinner(String winner) {
        firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey).child("winner")
                .setValue(winner).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        }
                    }
                });
    }


    @SuppressLint("SetTextI18n")
    private void setTurn() {
        if (!moves.getTurn().equals("")) {
            binding.mainTurnTv.setText("Turn: " + moves.getTurn());
        } else {
            binding.mainTurnTv.setText("Error: try to reconnect");
        }
    }


    private boolean checkUsername() {
        try {
            connectUsername = Objects.requireNonNull(binding.mainUsername.getText()).toString();
            if (!connectUsername.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter valid username", Toast.LENGTH_SHORT).show();
        return false;
    }


    // game program........ -==================================================//


//    private void setIconsOnBoard(int index) {
//        if (host) { // seat circle
//            boxes[index].setImageResource(R.drawable.player_1);
//        } else { // seat cross
//            boxes[index].setImageResource(R.drawable.player_2);
//        }
//        boxes[index].setVisibility(View.VISIBLE);
//


    private void setGamePlayMoves(int value) {

        // fit the values into the moves


        // for uploading content

        if (isOnline) {
            if (moves.getGameMoves().contains(value)) {
                Toast.makeText(this, "Already filled", Toast.LENGTH_SHORT).show();
                return;
            }
            moves.setTouchCount(moves.getTouchCount() + 1);


//        setIconsOnBoard(value);

            moves.setGameMoves(moves.getTouchCount(), value);

            if (moves.getTurn().equals(clientUsername)) {
                moves.setTurn(hostUsername);
            } else {
                moves.setTurn(clientUsername);

            }

            firebaseDatabase.getReference().child("GameSpace").child(gameSpaceKey).setValue(moves);
        } else {
            Toast.makeText(this, "User is not active", Toast.LENGTH_SHORT).show();
        }


    }


    View.OnClickListener box1Listener = v -> {

        if (checkAllData()) {
            setGamePlayMoves(0);
        }

    };
    View.OnClickListener box2Listener = v -> {
        if (checkAllData()) {
            setGamePlayMoves(1);
        }
    };
    View.OnClickListener box3Listener = v -> {
        if (checkAllData()) {
            setGamePlayMoves(2);
        }
    };
    View.OnClickListener box4Listener = v -> {
        if (checkAllData()) {
            setGamePlayMoves(3);
        }
    };
    View.OnClickListener box5Listener = v -> {
        if (checkAllData()) {
            setGamePlayMoves(4);
        }
    };
    View.OnClickListener box6Listener = v -> {
        if (checkAllData()) {
            setGamePlayMoves(5);
        }
    };
    View.OnClickListener box7Listener = v -> {
        if (checkAllData()) {
            setGamePlayMoves(6);
        }
    };
    View.OnClickListener box8Listener = v -> {
        if (checkAllData()) {
            setGamePlayMoves(7);
        }
    };
    View.OnClickListener box9Listener = v -> {
        if (checkAllData()) {
            setGamePlayMoves(8);
        }
    };

    private boolean checkAllData() {

        if (gameSpaceKey == null) {
            Toast.makeText(this, "You're not yet connected", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (moves.getTurn().equals("")) {
            Toast.makeText(this, "Error: try to reconnect", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!gameOver) {
            if (moves.getTurn().equals(currentUsername)) {
                return true;

            } else {
                Toast.makeText(this, "It is not your turn", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Game over", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private boolean checkGameOverStatus() {
        boolean gameOver = true;

        for (int i = 0; i < 9; i++) {
            if (moves.getGameMoves().get(i) == -1) {
                gameOver = false;
                break;
            }
        }

        return gameOver;
    }

    private boolean checkWinStatus() {


        boolean rowCheck = (player1Moves.contains(0) && player1Moves.contains(1) && player1Moves.contains(2)) ||
                (player1Moves.contains(3) && player1Moves.contains(4) && player1Moves.contains(5)) ||
                (player1Moves.contains(6) && player1Moves.contains(7) && player1Moves.contains(8));

        boolean columnCheck = (player1Moves.contains(0) && player1Moves.contains(3) && player1Moves.contains(6)) ||
                (player1Moves.contains(1) && player1Moves.contains(4) && player1Moves.contains(7)) ||
                (player1Moves.contains(2) && player1Moves.contains(5) && player1Moves.contains(8));
        boolean diagonalCheck = (player1Moves.contains(0) && player1Moves.contains(4) && player1Moves.contains(8)) ||
                (player1Moves.contains(2) && player1Moves.contains(4) && player1Moves.contains(6));

        if (rowCheck || columnCheck || diagonalCheck) {
            updateWinner(moves.getFirstMove());
            Log.i("winner1", "host : " + hostUsername);
            setLocalMovesToNeg1();

            return true;
        }


        rowCheck = (player2Moves.contains(0) && player2Moves.contains(1) && player2Moves.contains(2)) ||
                (player2Moves.contains(3) && player2Moves.contains(4) && player2Moves.contains(5)) ||
                (player2Moves.contains(6) && player2Moves.contains(7) && player2Moves.contains(8));

        columnCheck = (player2Moves.contains(0) && player2Moves.contains(3) && player2Moves.contains(6)) ||
                (player2Moves.contains(1) && player2Moves.contains(4) && player2Moves.contains(7)) ||
                (player2Moves.contains(2) && player2Moves.contains(5) && player2Moves.contains(8));
        diagonalCheck = (player2Moves.contains(0) && player2Moves.contains(4) && player2Moves.contains(8)) ||
                (player2Moves.contains(2) && player2Moves.contains(4) && player2Moves.contains(6));

        if (rowCheck || columnCheck || diagonalCheck) {

            if (moves.getFirstMove().equals(hostUsername)) {
                updateWinner(clientUsername);
            } else if (moves.getFirstMove().equals(clientUsername)) {
                updateWinner(hostUsername);
            }
            setLocalMovesToNeg1();
            return true;
        }

//        boolean rowCheck = (t1 == 1 && t2 == 1 && t3 == 1) || (t4 == 1 && t5 == 1 && t6 == 1) || (t7 == 1 && t8 == 1 && t9 == 1);
//        boolean columnCheck = (t1 == 1 && t4 == 1 && t7 == 1) || (t2 == 1 && t5 == 1 && t8 == 1) || (t3 == 1 && t6 == 1 && t9 == 1);
//        boolean diagonalCheck = (t1 == 1 && t5 == 1 && t9 == 1) || (t3 == 1 && t5 == 1 && t7 == 1);
//
//
        return false;
    }

    @Override
    protected void onDestroy() {
        setUserStatus(false);
        destroyConnection();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        setUserStatus(false);
        destroyConnection();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        setUserStatus(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        setUserStatus(true);
        super.onResume();
    }

    @Override
    protected void onStart() {
        setUserStatus(true);
        super.onStart();
    }

    @Override
    public void restartGame() {

        if (moves.getFirstMove().equals(hostUsername)) {
            moves.setFirstMove(clientUsername);
            moves.setSecondMove(hostUsername);
            moves.setTurn(clientUsername);
        } else {
            moves.setSecondMove(clientUsername);
            moves.setFirstMove(hostUsername);
            moves.setTurn(hostUsername);

        }

        isRestartGame = true;
        playAgain();
    }

    @Override
    public void dialogBackPressed() {

//        finish();
    }

    void stopSound() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void playWin() {
        stopSound();
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.win);

        startM();
    }

    void playLost() {
        stopSound();
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.lost_match);

        startM();
    }

    void playDraw() {
        stopSound();
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.draw_match);

        startM();
    }

//    void playClick() {
//        stopSound();
//        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.click2);
//
//        startM();
//    }

    void startM() {
        if (sounds) {
            try {
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_app:
                // go to about activity
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.sounds:
                sounds = !sounds;
                if (sounds) {
                    item.setTitle("Sounds off");
                } else {
                    item.setTitle("Sounds on");
                }
                break;
            case R.id.log_out:
                setUserStatus(false);
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}
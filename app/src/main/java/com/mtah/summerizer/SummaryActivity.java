package com.mtah.summerizer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.mtah.summerizer.db.SummaryDBHelper;
import com.mtah.tools.Grapher;
import com.mtah.tools.PreProcessor;

import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity implements SaveDialog.SaveDialogListener {
    private static final String TAG = "SummaryActivity";
    private String documentText;
    private final String EMPTY_MESSAGE = "Summary not available";
    private PreProcessor preProcessor = HomeActivity.preProcessor;
    private Grapher grapher = HomeActivity.grapher;
    private Button saveSummaryButton;
    private String summaryText;
    private String saveSummaryName;
    private SummaryDBHelper dbHelper;
    public SQLiteDatabase summaryDatabase;
    private Intent textIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        dbHelper = new SummaryDBHelper(this);

        saveSummaryButton = findViewById(R.id.saveButton);
        saveSummaryButton.setEnabled(false);
        saveSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSaveDialog();

            }
        });

        TextView summaryTextView = findViewById(R.id.summaryTextView);
        textIntent = getIntent();
        if (textIntent.hasExtra("docText")) {
            documentText = textIntent.getStringExtra("docText");
            Log.i(TAG, "onCreate: DOC TEXT:" + documentText);

            if (documentText == null || documentText.isEmpty()) {
                Toast.makeText(this, "Not document text available", Toast.LENGTH_SHORT).show();
                summaryTextView.setText(EMPTY_MESSAGE);
                saveSummaryButton.setEnabled(false);
            } else {
                summaryText = summaryTool(documentText).replaceAll("    ", " ");
            }
            if (summaryText == null || summaryText.isEmpty()) {
                summaryTextView.setText(EMPTY_MESSAGE);
                saveSummaryButton.setEnabled(false);
            } else {
                saveSummaryButton.setEnabled(true);
            }
        } else if (textIntent.hasExtra("open")){
            summaryText = textIntent.getStringExtra("open");
        }
        summaryTextView.setText(summaryText);
        summaryTextView.setMovementMethod(new ScrollingMovementMethod());

    }



    //summarize the text to the least between  1/4 th of the size or 15 sentences
    private String summaryTool(String documentText) {
        StringBuilder text = new StringBuilder();
        String[] sentences = preProcessor.extractSentences(documentText.trim());
        Log.i(TAG, "summerizedDocument: No of sentences: " + sentences.length);
        ArrayList<Grapher.SentenceVertex> sentenceVertices = grapher.sortSentences(sentences, preProcessor.tokenizeSentences(sentences));
        int summarySize = ((sentences.length * 25) / 100);
        int maxLenght = 15;
        int counter = 0;
        for (int i = 0; i < summarySize; i++) {
            if (i < maxLenght) {

                text.append(sentenceVertices.get(i).getSentence().trim());
                text.append(" ");
                counter++;
            } else
                break;
        }
        Log.i(TAG, "summerizedDocument: Summary length = " + counter + " sentences");
        Log.i(TAG, "\nSUMMARY:\n" + text.toString());
        return text.toString();
    }

    //Dialog for summary name input for saving the sumarry
    private void openSaveDialog(){
        if (textIntent.hasExtra("open")){
            Toast.makeText(this, "This has already been saved", Toast.LENGTH_SHORT).show();
        }
        SaveDialog saveDialog = new SaveDialog();
        saveDialog.show(getSupportFragmentManager(), "save dialog");
    }

    @Override
    public void applyText(String name) {
        //summary save name
        saveSummaryName = name;
        Log.i(TAG, "applyText: Save name: " + saveSummaryName);

        Log.i(TAG, "onClick: GOT HERE, SAVING");
        if (saveSummaryName != null && !saveSummaryName.isEmpty()) {
            try {
                saveSummary(saveSummaryName, summaryText);
                saveSummaryButton.setEnabled(false);
                Log.i(TAG, "onClick: Save successful");
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "applyText: ", e);
                Toast.makeText(this, "Could not save summary", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SummaryActivity.this, "Try again, Enter save name", Toast.LENGTH_SHORT).show();
        }
    }

    //save a summary to database
    private void saveSummary (String summaryName, String summaryText) throws Exception{
        dbHelper.saveSummary(summaryName, summaryText);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    public void shareSummary(View view) {
    }
}

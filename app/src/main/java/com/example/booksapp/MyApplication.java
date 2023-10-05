package com.example.booksapp;

import static com.example.booksapp.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.booksapp.adapters.AdapterPdfAdmin;
import com.example.booksapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    
    private static final String TAG_DOWNLOAD="DOWNLOAD_TAG";
    @Override
    public void onCreate() {
        super.onCreate();
    }
    public static final String formatTimestamp(long timestamp){
        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        String date= DateFormat.format("dd/MM/yyyy",cal).toString();
        return date;
    }
    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG="DELETE_BOOK_TAG";
        Log.d(TAG,"deleteBook: Deleting");
        Log.d(TAG,"deleteBook: Deleting book from storage ");
        StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG,"onSuccess: Deleted from storage");
                Log.d(TAG,"onSuccess: Now deleting info from db");

                DatabaseReference reference= FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Books");
                reference.child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Deleted from db too");
                        Toast.makeText(context, "Book deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: Failed to delete from db due to "+e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onFailure: Failed to delete from storage due to "+e.getMessage());
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG="PDF_SIZE_TAG";
        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                double bytes=storageMetadata.getSizeBytes();
                Log.d(TAG,"onSuccess: "+pdfTitle+" "+bytes);
                double kb=bytes/1024;
                double mb=kb/1024;

                if(mb>=1){
                   sizeTv.setText(String.format("%.2f",mb)+" MB");
                } else if (kb>=1) {
                   sizeTv.setText(String.format("%.2f",kb)+" KB");
                }else{
                   sizeTv.setText(String.format("%.2f",bytes)+" bytes");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onFailure: "+e.getMessage());
            }
        });
    }
    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar) {
        String TAG="PDF_LOAD_SINGLE_TAG";
        StorageReference ref=FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG,"onSuccess: "+pdfTitle+"successfully got the file ");
                pdfView.fromBytes(bytes).pages(0).spacing(0).swipeHorizontal(false).enableSwipe(false).onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG,"onError: "+t.getMessage());
                    }
                }).onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        Log.d(TAG, "onPageError: " + t.getMessage());
                    }
                }).onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        Log.d(TAG, "onLoadComplete: pdf loaded");
                    }
                }).load();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                Log.d(TAG,"onFailure: failed getting file from url due to "+e.getMessage());
            }
        });
    }
    public static void loadCategory(String categoryId,TextView categoryTv) {
        DatabaseReference ref= FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Categories");
        ref.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String category=""+snapshot.child("category").getValue();

                categoryTv.setText(category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void incrementBookViewCount(String bookId){
        DatabaseReference ref=FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String viewsCount=""+snapshot.child("viewsCount").getValue();
                if(viewsCount.equals("") || viewsCount.equals("null")){
                    viewsCount="0";
                }
                long newViewsCount=Long.parseLong(viewsCount)+1;
                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put("viewsCount",newViewsCount);

                DatabaseReference ref=FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Books");
                ref.child(bookId).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
}

    public static void downloadBook(Context context,String bookId,String bookTitle,String bookUrl){
        Log.d(TAG_DOWNLOAD,"downloadBook: downloading book");
        String nameWithExtension=bookTitle+".pdf";
        Log.d(TAG_DOWNLOAD,"downloadBook: Name:"+nameWithExtension);
        
        StorageReference storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG_DOWNLOAD,"onSuccess: book downloaded");
                saveDownloadedBook(context,bytes,nameWithExtension,bookId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG_DOWNLOAD,"onFailure: failed to download due to"+e.getMessage());
                Toast.makeText(context, "Failed to download due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void saveDownloadedBook(Context context, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD,"saveDownloadedBook: saving downloaded book");
        try {
            File downloadsFolder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filepath=downloadsFolder.getPath()+"/"+nameWithExtension;
            FileOutputStream out=new FileOutputStream(filepath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to download folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD,"saveDownloadedBook:Saved to download folder");
            incrementBookDownloadCount(bookId);
        }catch (Exception e){
            Log.d(TAG_DOWNLOAD,"saveDownloadedBook:Failed saving to download folder due to"+e.getMessage());
            Toast.makeText(context, "Failed saving to download folder due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD,"incrementBookDownloadCount:Increasing book download count");
        DatabaseReference ref=FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String downloadsCount=""+snapshot.child("downloadsCount").getValue();
                Log.d(TAG_DOWNLOAD,"onDataChange:download count"+downloadsCount);

                if(downloadsCount.equals("")||downloadsCount.equals("null")){
                    downloadsCount="0";
                }
                long newDownloadsCount=Long.parseLong(downloadsCount)+1;
                Log.d(TAG_DOWNLOAD,"onDataChange:new downloads count"+newDownloadsCount);

                HashMap<String,Object>  hashMap=new HashMap<>();
                hashMap.put("downloadsCount",newDownloadsCount);

                DatabaseReference reference=FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Books");
                reference.child(bookId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG_DOWNLOAD,"onSuccess:downloads count updated");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD,"onFailure:failed to update downloads count due to"+e.getMessage());

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}

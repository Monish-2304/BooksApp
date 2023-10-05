package com.example.booksapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.MyApplication;
import com.example.booksapp.activities.PdfDetailActivity;
import com.example.booksapp.activities.PdfEditActivity;
import com.example.booksapp.databinding.RowPdfAdminBinding;
import com.example.booksapp.filters.FilterPdfAdmin;
import com.example.booksapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    private Context context;
    public ArrayList<ModelPdf> pdfArrayList,filterList;
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;
    private static final String TAG="PDF_ADAPTER_TAG";

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList=pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {

        ModelPdf model=pdfArrayList.get(position);
        String pdfId= model.getId();
        String categoryId= model.getCategoryId();
        String title= model.getTitle();
        String description= model.getDescription();
        long timestamp=model.getTimestamp();
        String pdfUrl= model.getUrl();
        String formattedDate= MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        MyApplication.loadCategory(""+categoryId,holder.categoryTv);
        MyApplication.loadPdfFromUrlSinglePage(""+pdfUrl,""+title,holder.pdfView,holder.progressBar);
        MyApplication.loadPdfSize(""+pdfUrl,""+title,holder.sizeTv);

        //changes today
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDialog(model,holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });

    }

    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId= model.getId();
        String bookUrl= model.getUrl();
        String bookTitle= model.getTitle();
        String[] options={"Edit","Delete"};

        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Choose Options").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    Intent intent=new Intent(context, PdfEditActivity.class);
                    intent.putExtra("bookId",bookId);
                    context.startActivity(intent);
                }else if(i==1){
                    MyApplication.deleteBook(context,""+bookId,""+bookUrl,""+bookTitle);
                }
            }
        }).show();
    }


    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        ImageButton moreBtn;
        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView=binding.pdfView;
            progressBar=binding.progressBar;
            titleTv=binding.titleTv;
            descriptionTv=binding.descriptionTv;
            categoryTv=binding.categoryTv;
            sizeTv=binding.sizeTv;
            dateTv=binding.dateTv;
            moreBtn=binding.moreBtn;
        }
    }
}

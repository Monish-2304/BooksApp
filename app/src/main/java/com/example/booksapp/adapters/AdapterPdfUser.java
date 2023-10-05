package com.example.booksapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.MyApplication;
import com.example.booksapp.activities.PdfDetailActivity;
import com.example.booksapp.databinding.RowPdfUserBinding;
import com.example.booksapp.filters.FilterPdfUser;
import com.example.booksapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;
import java.util.List;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList,filterList;
    private FilterPdfUser filter;
    private RowPdfUserBinding binding;

    private static final String TAG="ADAPTER_PDF_USER_TAG";

    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        if (pdfArrayList == null) {
            this.pdfArrayList = new ArrayList<>(); // Initialize an empty ArrayList
        } else {
            this.pdfArrayList = pdfArrayList;
        }
        this.filterList = new ArrayList<>(this.pdfArrayList);
    }

    public void setPdfArrayList(ArrayList<ModelPdf> pdfArrayList) {
        if (pdfArrayList == null) {
            this.pdfArrayList = new ArrayList<>(); // Initialize an empty ArrayList
        } else {
            this.pdfArrayList = pdfArrayList;
        }
        this.filterList = new ArrayList<>(this.pdfArrayList); // Update filterList with a copy of the new pdfArrayList
        notifyDataSetChanged();
    }
    public AdapterPdfUser(@NonNull View itemView) {

    }

    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPdfUser.HolderPdfUser holder, int position) {
ModelPdf model=pdfArrayList.get(position);
String bookId=model.getId();
String title= model.getTitle();
String description=model.getDescription();
String pdfUrl= model.getUrl();
String categoryId= model.getCategoryId();
long timestamp= model.getTimestamp();

String date= MyApplication.formatTimestamp(timestamp);

holder.titleTv.setText(title);
holder.descriptionTv.setText(description);
holder.dateTv.setText(date);
MyApplication.loadPdfFromUrlSinglePage(""+pdfUrl,""+title,holder.pdfView,holder.progressBar);
MyApplication.loadCategory(""+categoryId,holder.categoryTv);
MyApplication.loadPdfSize(""+pdfUrl,""+title,holder.sizeTv);

holder.itemView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent=new Intent(context, PdfDetailActivity.class);
        intent.putExtra("bookId",bookId);
        context.startActivity(intent);
    }
});

    }

    @Override
    public int getItemCount() {
        return pdfArrayList != null ? pdfArrayList.size() : 0;
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterPdfUser(filterList,this);
        }
        return filter;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder{
        public ProgressBar progressBar;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        PDFView pdfView;
        public HolderPdfUser(@NonNull View itemView){
            super(itemView);
            titleTv=binding.titleTv;
            descriptionTv=binding.descriptionTv;
            categoryTv=binding.categoryTv;
            sizeTv=binding.sizeTv;
            dateTv=binding.dateTv;
            pdfView=binding.pdfView;
        }
    }
}

package com.example.android.cgpacalculator.ui;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import com.example.android.cgpacalculator.MainViewModel;
import com.example.android.cgpacalculator.R;
import com.example.android.cgpacalculator.database.tables.Sgpa;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;


public class ReportFragment extends Fragment {

    MainViewModel viewModel;
    RecyclerView sgpaSemListRecyclerView;
    TextView student_cgpa;
    TextView cgpa_percentage;
    ImageButton btnPDF;
    private static String FILE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnPDF = view.findViewById(R.id.btnPDF);
        student_cgpa = view.findViewById(R.id.student_cgpa);
        cgpa_percentage = view.findViewById(R.id.cgpa_percentage);

        btnPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String semID = "1";
                String semSgpa = "4.95";
                String semPoints = "560";

                try {
                    createPDF(semID,semSgpa,semPoints);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });

        viewModel.getStudentCgpa().observe(getViewLifecycleOwner(), cgpas -> {
            double tempCgpa;
            String percentage;
            if (cgpas.size() == 0 || cgpas.get(0).getCgpa() == 0) {
                tempCgpa = 0.0;
                percentage = String.valueOf(0.0);
            } else {
                tempCgpa = Math.round(cgpas.get(0).getCgpa() * 100.0) / 100.0;
                percentage = "(" + String.format(Locale.ENGLISH, "%.2f", (tempCgpa - 0.75) * 10) + "%)";
            }
            student_cgpa.setText(String.format(Locale.ENGLISH, "%.2f", tempCgpa));
            cgpa_percentage.setText(percentage);
        });

        sgpaSemListRecyclerView = view.findViewById(R.id.recycler_view_sem);
        sgpaSemListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sgpaSemListRecyclerView.setHasFixedSize(true);

        SemListAdapter adapter = new SemListAdapter();
        sgpaSemListRecyclerView.setAdapter(adapter);
        adapter.navigationListener = (sgpa) -> {
            Bundle bundle = new Bundle();
            bundle.putInt("semId", sgpa.getSemId());
            Navigation.findNavController(view).navigate(R.id.to_marksFragment, bundle);
        };

        viewModel.getAllSemLiveData().observe(getViewLifecycleOwner(), adapter::setAllSemSgpaList);
    }

    private void createPDF(String semID, String semSgpa, String semPoints) throws FileNotFoundException {
        if(student_cgpa.getText().toString().equals("0.00")){
            Toast.makeText(getActivity(),"No Data", Toast.LENGTH_SHORT).show();
        }
        else {

            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File file = new File(pdfPath, "semReport.pdf");
            OutputStream outputStream = new FileOutputStream(file);

            Toast.makeText(getActivity(),"PDF Generated", Toast.LENGTH_SHORT).show();

            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));

                document.open();

                document.addTitle("Semester Report");
                document.addSubject("via CGPA Calculator");
                document.addAuthor("Ritik Raj Chauhan, AIEMS");
                document.addCreator("Ritik Raj Chauhan, AIEMS");

                Paragraph preface = new Paragraph();

                addEmptyLine(preface, 1);

                preface.add(new Paragraph("Semester Report", catFont));

                addEmptyLine(preface, 1);

                preface.add(new Paragraph(
                        "Your CGPA : (" + student_cgpa.getText().toString() + " CGPA)", smallBold));
                addEmptyLine(preface, 1);
                preface.add(new Paragraph(
                        "Your Percentage : " + cgpa_percentage.getText().toString() , smallBold));

                addEmptyLine(preface, 8);

                document.add(preface);

                document.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity()," Stacktrace", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

}
package com.example.roombasic;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;


public class AddFragment extends Fragment {

    private Button buttonAdd;
    private EditText et_English,et_Chinese;
    private WordViewModel wordViewModel;

    public AddFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity =requireActivity();
        buttonAdd = activity.findViewById(R.id.bt_addword);
        et_English = activity.findViewById(R.id.et_english);
        et_Chinese = activity.findViewById(R.id.et_chinese);
        wordViewModel = new ViewModelProvider(activity).get(WordViewModel.class);

        //默認按鈕暫時不可按
        buttonAdd.setEnabled(false);

        //聚焦editTextEnglish
        et_English.requestFocus();

        //用InputMethodManager來show鍵盤，並專注在et_English上。
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_English,0);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //trim() 可以去除頭尾的空白
                String english = et_English.getText().toString().trim();
                String chinese = et_Chinese.getText().toString().trim();
                buttonAdd.setEnabled(!english.isEmpty() && !chinese.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        et_English.addTextChangedListener(textWatcher);
        et_Chinese.addTextChangedListener(textWatcher);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String english = et_English.getText().toString().trim();
                String chinese = et_Chinese.getText().toString().trim();
                Word word = new Word(english,chinese);
                wordViewModel.insertWords(word);
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();

//                //用InputMethodManager來hide鍵盤，調用View內的getWindowToken()。
//                InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        });
    }
}
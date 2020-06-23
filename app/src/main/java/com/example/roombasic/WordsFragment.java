package com.example.roombasic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SharedMemory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class WordsFragment extends Fragment {

    private WordViewModel wordViewModel;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter1, myAdapter2;
    private FloatingActionButton fab;
    private LiveData<List<Word>> filteredWords;
    private static final String VIEW_TYPE_SHP = "view_type_shp";
    private static final String IS_USING_CARD_VIEW = "is_Using_CardView";

    public WordsFragment() {
        // 默認工具條
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wordViewModel = new ViewModelProvider(requireActivity()).get(WordViewModel.class);
        recyclerView = requireActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        myAdapter1 = new MyAdapter(false, wordViewModel);
        myAdapter2 = new MyAdapter(true, wordViewModel);

        //讀取使用者設定初始化視圖類型
        SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
        boolean isUsingCardView = shp.getBoolean(IS_USING_CARD_VIEW, false);
        if (isUsingCardView) {
            recyclerView.setAdapter(myAdapter2);
        } else {
            recyclerView.setAdapter(myAdapter1);
        }

        //初始化時不過濾，顯示所有資料，並設定filteredWords自身設定觀察，動態改動資料(其變動會在SearchView監聽)
        filteredWords = wordViewModel.getAllWordsLive();
        filteredWords.observe(requireActivity(), new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                int temp = myAdapter1.getItemCount();
                myAdapter1.setAllWords(words);
                myAdapter2.setAllWords(words);
                if (temp != words.size()) {
                    myAdapter2.notifyDataSetChanged();
                    myAdapter1.notifyDataSetChanged();
                }
            }
        });

        //懸浮按鈕切換頁面
        fab = requireActivity().findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_wordsFragment_to_addFragment);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            /*項目一 : 清空數據被點擊*/
            case R.id.mu_cleardata:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("清空數據");
                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        wordViewModel.deleteAllWords();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.create();
                builder.show();
                break;
            /*項目二 : 切換數據被點擊,並把用戶設定存下來*/
            case R.id.mu_switchview:
                SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
                boolean isUsingCardView = shp.getBoolean(IS_USING_CARD_VIEW, false);
                SharedPreferences.Editor editor = shp.edit();
                if (isUsingCardView) {
                    recyclerView.setAdapter(myAdapter1);
                    editor.putBoolean(IS_USING_CARD_VIEW, false);
                } else {
                    recyclerView.setAdapter(myAdapter2);
                    editor.putBoolean(IS_USING_CARD_VIEW, true);
                }
                editor.apply();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setMaxWidth(700);

        /*設定searchView 隨時監聽輸入，返回值true則停止處理*/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
        /*按下Enter時觸發*/
            public boolean onQueryTextSubmit(String query) {
                Log.d("myLo", "submit!! = " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("myLog", "onQueryTextChange = " + newText);
                String patten = newText.trim();
                filteredWords.removeObservers(requireActivity());
                filteredWords = wordViewModel.findWordsWithPatten(patten);
                filteredWords.observe(requireActivity(), new Observer<List<Word>>() {
                    @Override
                    public void onChanged(List<Word> words) {
                        int temp = myAdapter1.getItemCount();
                        myAdapter1.setAllWords(words);
                        myAdapter2.setAllWords(words);
                        if (temp != words.size()) {
                            myAdapter2.notifyDataSetChanged();
                            myAdapter1.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    /*只要回到這個頁面，就收起鍵盤*/
    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}
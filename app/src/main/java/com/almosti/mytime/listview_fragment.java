package com.almosti.mytime;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class listview_fragment extends Fragment {
    private  MainActivity.ListViewAdapter listviewadapter;
    callbackvalue callback;

    public listview_fragment(MainActivity.ListViewAdapter theListViewAdapter) {
        listviewadapter=theListViewAdapter;
    }

    public interface callbackvalue {
        public void sendvalue(int selecteditem);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (callbackvalue) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_listview, container, false);
        ListView time_listview = (ListView) contentView.findViewById(R.id.time_list);
        time_listview.setAdapter(listviewadapter);
        time_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callback.sendvalue(position);
            }
        });

        return contentView;
    }
}

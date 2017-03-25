package com.example.webert3.imagedisplayapp.Fragments;

/**
 * Created by webert3 on 5/19/16.
 */
import com.example.webert3.imagedisplayapp.Helpers.CaldroidCustomAdapter;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

public class CaldroidCustomFragment extends CaldroidFragment {

    @Override
    public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
        // TODO Auto-generated method stub
        return new CaldroidCustomAdapter(getActivity(), month, year,
                getCaldroidData(), extraData);
    }

}

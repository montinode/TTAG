package com.dynamixsoftware.printingsample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class WireframeFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wireframe, container, false);
        root.findViewById(R.id.wireframe_print_layout).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wireframe_print_layout) {
            // Wireframe visualization action - demonstrates UI structure
            // In a real implementation, this would show a wireframe preview
            // of the print layout or document structure
        }
    }
}

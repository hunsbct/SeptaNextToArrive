package com.example.codyhunsberger.nexttoarrive;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class OutputFragment extends Fragment {

    private NavigationListener listener;

    public interface NavigationListener {
        void onButtonPress(int index);
    }

    public OutputFragment() {

    }
    public static OutputFragment newInstance(String[] jsonData, int index, int jsonArrayLength) {
        OutputFragment fragment = new OutputFragment();
        Bundle args = new Bundle();
        args.putStringArray("json", jsonData);
        args.putInt("index", index);
        args.putInt("length", jsonArrayLength);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_output, container, false);

        final int index = getArguments().getInt("index");
        String resultNumber;
        String[] data = getArguments().getStringArray("json");

        TextView resultNumberTv = (TextView) v.findViewById((R.id.resultNumberTv));
        TextView trainNumberTv = (TextView) v.findViewById(R.id.trainNumberTv);
        TextView lineTv = (TextView) v.findViewById(R.id.lineTv);
        TextView departureTimeTv = (TextView) v.findViewById(R.id.departureTimeTv);
        TextView arrivalTimeTv = (TextView) v.findViewById(R.id.arrivalTimeTv);
        TextView statusValueTv = (TextView) v.findViewById(R.id.status_value_tv);
        TextView delayTv = (TextView) v.findViewById(R.id.delayTv);
        TextView transferTv = (TextView) v.findViewById(R.id.transferTv);

        switch(index) {
            case 0:
                resultNumber = "Next";
                break;
            case 1:
                resultNumber = "2nd";
                break;
            case 2:
                resultNumber = "3rd";
                break;
            case 3:
                resultNumber = "4th";
                break;
            case 4:
                resultNumber = "5th";
                break;
            case 5:
                resultNumber = "6th";
                break;
            case 6:
                resultNumber = "7th";
                break;
            case 7:
                resultNumber = "8th";
                break;
            case 8:
                resultNumber = "9th";
                break;
            case 9:
                resultNumber = "10th";
                break;
            default:
                resultNumber = "An upcoming";
                break;
        }

        resultNumberTv.setText(getResources().getString(R.string.result_number_label, resultNumber));
        trainNumberTv.setText(getResources().getString(R.string.train_number_label, data[0]));
        lineTv.setText(getResources().getString(R.string.line_label, data[1]));
        departureTimeTv.setText(getResources().getString(R.string.departure_time__label, data[2]));
        arrivalTimeTv.setText(getResources().getString(R.string.arrival_time_label, data[3]));
        // Dynamic label change if train is late vs on time
        if (data[4].equalsIgnoreCase("on time")) {
            statusValueTv.setTextColor(Color.GREEN);
            statusValueTv.setText(" " + data[4].toUpperCase());
            delayTv.setText("");
        }
        else {
            String[] sentence = data[4].split(" ");
            if (sentence[0].equals("1")) {
                delayTv.setText(getResources().getString(R.string.delay_label_singular));
            }
            else {
                delayTv.setText(getResources().getString(R.string.delay_label_plural));
            }
            statusValueTv.setTextColor(Color.RED);
            statusValueTv.setText(" " + sentence[0] + " ");
        }
        transferTv.setText(getResources().getString(R.string.transfer_label, data[5]));

        Button back_button = (Button) v.findViewById(R.id.back_button);
        Button next_button = (Button) v.findViewById(R.id.next_button);

        if (index < 1) {
            back_button.setEnabled(false);
        }
        if (index == getArguments().getInt("length") - 1) {
            next_button.setEnabled(false);
        }

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonPress(index - 1);
            }
        });

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonPress(index + 1);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (NavigationListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}

// todo display time until next train arrives by subtracting current time
package com.squareup.kindphotobot.printer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.squareup.kindphotobot.R;
import java.util.List;

import static java.util.Collections.emptyList;

public class PrinterListAdapter extends BaseAdapter {
  private List<CloudPrintService.Printer> printers = emptyList();

  @Override public int getCount() {
    return printers.size();
  }

  public void updatePrinterList(List<CloudPrintService.Printer> printers) {
    this.printers = printers;
    notifyDataSetChanged();
  }

  @Override public CloudPrintService.Printer getItem(int position) {
    return printers.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    Context context = parent.getContext();
    TextView textView;
    if (convertView == null) {
      textView = (TextView) LayoutInflater.from(context)
          .inflate(android.R.layout.simple_list_item_1, parent, false);
    } else {
      textView = (TextView) convertView;
    }
    CloudPrintService.Printer printer = getItem(position);
    textView.setText(
        context.getString(R.string.printer_name_format, printer.description, printer.ownerName));
    return textView;
  }
}

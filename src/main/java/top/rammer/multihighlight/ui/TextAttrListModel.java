package top.rammer.multihighlight.ui;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.swing.SortOrder;

import top.rammer.multihighlight.config.NamedTextAttr;

/**
 * Created by Rammer on 07/02/2017.
 */
public class TextAttrListModel extends ListTableModel<NamedTextAttr> {

    public TextAttrListModel(List<NamedTextAttr> items) {
        super(new ColumnInfo[]{ new NamedColumnInfo() }, items, 0, SortOrder.UNSORTED);
    }

    private static class NamedColumnInfo extends ColumnInfo<NamedTextAttr, String> {

        private NamedColumnInfo() {
            super("name");
        }

        @Nullable
        @Override
        public String valueOf(NamedTextAttr namedTextAttr) {
            return namedTextAttr.getName();
        }

        @Override
        public void setValue(NamedTextAttr namedTextAttr, String value) {
            if (value != null) {
                namedTextAttr.setName(value);
            }
        }
    }
}

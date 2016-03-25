package org.masterylearning.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.masterylearning.domain.data.EntryData;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Entity (name = "Entry")
public class Entry implements Child<Entry>, Container<Entry> {

    @Id
    @GeneratedValue
    public Long id;

    @Column (name = "`index`")
    public int index;

    @JsonBackReference ("course-child")
    @OneToOne
    public Course course;

    public int depth;

    @JsonManagedReference("entry-data")
    @OneToOne (mappedBy = "container", cascade = CascadeType.PERSIST)
    public EntryData data;

    @JsonBackReference ("entry-child")
    @ManyToOne
    public Entry parent;

    @JsonManagedReference("entry-child")
    @OneToMany (mappedBy = "parent", cascade = CascadeType.PERSIST)
    public List<Entry> children = new ArrayList<> ();

    public Entry () {}

    public Entry (EntryData entryData) {
        this.data = entryData;
    }

    public Entry next () {
        List<Entry> children = getChildren ();
        if (children.size () > 0)
            return children.get (0);
        else if (this.hasNextSibling())
            return this.nextSibling();
        else {
            Container<Entry> parent = this.getParent ();
            return parent.nextUpwards ();
        }
    }

    public Entry nextUpwards () {
        if (hasNextSibling ())
            return nextSibling ();
        else {
            Container<Entry> parent = this.getParent ();
            if (parent != null) {
                return parent.nextUpwards ();
            }
        }

        return null;
    }

    @Override
    public boolean hasNext () {
        if (this.children.size () > 0)
            return true;
        else if (this.hasNextSibling ())
            return true;
        else {
            Container<Entry> parent = this.getParent ();
            if (parent != null && parent instanceof Entry)
            {
                Entry entry = (Entry) parent;
                return entry.hasNext ();
            }
        }

        return false;
    }

    @Override
    public Entry nextSibling () {
        Container<Entry> parent = this.getParent ();
        if (parent != null)
        {
            List<Entry> children = parent.getChildren ();
            if (children.size () > this.index + 1)
                return children.get (this.index + 1);
        }

        return null;
    }


    @Override
    public boolean hasNextSibling () {
        Container<Entry> parent = this.getParent ();
        if (parent != null)
            if (parent.getChildren ().size () > this.index + 1)
                return true;

        return false;
    }

    @Override
    public Entry getInstance () {
        return this;
    }

    @Override
    public Container<Entry> getParent () {
        if (this.parent != null)
            return this.parent;
        else
            return this.course;
    }

    @Override
    public List<Entry> getChildren () {
        return children;
    }

    public Entry insert (EntryData entryData) {
        Entry entry = new Entry (entryData);
        this.children.add (entry);

        entry.index = this.children.size () - 1;
        entry.depth = this.depth + 1;
        entry.parent = this;

        entryData.container = entry;

        return entry;
    }
}

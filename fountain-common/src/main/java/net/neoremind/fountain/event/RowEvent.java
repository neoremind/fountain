package net.neoremind.fountain.event;

public interface RowEvent {
    boolean isInsert();

    boolean isUpdate();

    boolean isDelete();
}

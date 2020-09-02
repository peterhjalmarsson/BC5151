/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bc5151;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author peter
 */
public class CWUndoLetter implements UndoableEdit {

    int x;
    int y;
    char oldChar;
    char newChar;
    CrossWord crossWord;

    public CWUndoLetter(CrossWord crossWord, char oldChar, char newChar, int x, int y) {
        this.crossWord = crossWord;
        this.oldChar = oldChar;
        this.newChar = newChar;
        this.x = x;
        this.y = y;
    }

    @Override
    public void undo() throws CannotUndoException {
        if (crossWord.letter[x][y] != newChar) {
            throw new CannotUndoException();
        }
        crossWord.letter[x][y] = oldChar;
    }

    @Override
    public boolean canUndo() {
        return crossWord.letter[x][y] == newChar;
    }

    @Override
    public void redo() throws CannotRedoException {
        if (crossWord.letter[x][y] != oldChar) {
            throw new CannotRedoException();
        }
        crossWord.letter[x][y] = newChar;
    }

    @Override
    public boolean canRedo() {
        return crossWord.letter[x][y] == oldChar;
    }

    @Override
    public void die() {
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        return false;
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
        return false;
    }

    @Override
    public boolean isSignificant() {
        return false;
    }

    @Override
    public String getPresentationName() {
        return "Add letter";
    }

    @Override
    public String getUndoPresentationName() {
        return "Remove letter";
    }

    @Override
    public String getRedoPresentationName() {
        return "Add letter";
    }

}

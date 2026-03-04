package com.huanchengfly.tieba.post.ui.widgets.edittext

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.io.Serializable
import java.util.LinkedList

class OperationManager(private val editText: EditText) : TextWatcher {

    private val undoOpts = LinkedList<EditOperation>()
    private val redoOpts = LinkedList<EditOperation>()
    private var opt: EditOperation? = null
    private var enable = true

    fun disable(): OperationManager {
        enable = false
        return this
    }

    fun enable(): OperationManager {
        enable = true
        return this
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (count > 0 && enable) {
            val end = start + count
            if (opt == null) {
                opt = EditOperation()
            }
            opt?.setSrc(s.subSequence(start, end), start, end)
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (count > 0 && enable) {
            val end = start + count
            if (opt == null) {
                opt = EditOperation()
            }
            opt?.setDst(s.subSequence(start, end), start, end)
        }
    }

    override fun afterTextChanged(s: Editable) {
        if (enable && opt != null) {
            if (redoOpts.isNotEmpty()) {
                redoOpts.clear()
            }
            opt?.let { undoOpts.push(it) }
        }
        opt = null
    }

    fun canUndo(): Boolean = undoOpts.isNotEmpty()

    fun canRedo(): Boolean = redoOpts.isNotEmpty()

    fun undo(): Boolean {
        if (canUndo()) {
            val undoOpt = undoOpts.pop()
            disable()
            undoOpt.undo(editText)
            enable()
            redoOpts.push(undoOpt)
            return true
        }
        return false
    }

    fun redo(): Boolean {
        if (canRedo()) {
            val redoOpt = redoOpts.pop()
            disable()
            redoOpt.redo(editText)
            enable()
            undoOpts.push(redoOpt)
            return true
        }
        return false
    }

    fun exportState(): Bundle {
        val state = Bundle()
        state.putSerializable(KEY_UNDO_OPTS, undoOpts)
        state.putSerializable(KEY_REDO_OPTS, redoOpts)
        return state
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    fun importState(state: Bundle?) {
        val savedUndoOpts = state?.getSerializable(KEY_UNDO_OPTS) as? Collection<EditOperation>
        undoOpts.clear()
        savedUndoOpts?.let { undoOpts.addAll(it) }

        val savedRedoOpts = state?.getSerializable(KEY_REDO_OPTS) as? Collection<EditOperation>
        redoOpts.clear()
        savedRedoOpts?.let { redoOpts.addAll(it) }
    }

    private class EditOperation : Parcelable, Serializable {
        private var src: String? = null
        private var srcStart: Int = 0
        private var srcEnd: Int = 0
        private var dst: String? = null
        private var dstStart: Int = 0
        private var dstEnd: Int = 0

        constructor()

        private constructor(parcel: Parcel) {
            src = parcel.readString()
            srcStart = parcel.readInt()
            srcEnd = parcel.readInt()
            dst = parcel.readString()
            dstStart = parcel.readInt()
            dstEnd = parcel.readInt()
        }

        fun setSrc(src: CharSequence?, srcStart: Int, srcEnd: Int): EditOperation {
            this.src = src?.toString() ?: ""
            this.srcStart = srcStart
            this.srcEnd = srcEnd
            return this
        }

        fun setDst(dst: CharSequence?, dstStart: Int, dstEnd: Int): EditOperation {
            this.dst = dst?.toString() ?: ""
            this.dstStart = dstStart
            this.dstEnd = dstEnd
            return this
        }

        fun undo(text: EditText) {
            val editable = text.text ?: return
            var idx = -1
            if (dstEnd > 0) {
                editable.delete(dstStart, dstEnd)
                if (src == null) {
                    idx = dstStart
                }
            }
            val srcVal = src
            if (srcVal != null) {
                editable.insert(srcStart, srcVal)
                idx = srcStart + srcVal.length
            }
            if (idx >= 0) {
                text.setSelection(idx)
            }
        }

        fun redo(text: EditText) {
            val editable = text.text ?: return
            var idx = -1
            if (srcEnd > 0) {
                editable.delete(srcStart, srcEnd)
                if (dst == null) {
                    idx = srcStart
                }
            }
            val dstVal = dst
            if (dstVal != null) {
                editable.insert(dstStart, dstVal)
                idx = dstStart + dstVal.length
            }
            if (idx >= 0) {
                text.setSelection(idx)
            }
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(src)
            dest.writeInt(srcStart)
            dest.writeInt(srcEnd)
            dest.writeString(dst)
            dest.writeInt(dstStart)
            dest.writeInt(dstEnd)
        }

        companion object CREATOR : Parcelable.Creator<EditOperation> {
            override fun createFromParcel(source: Parcel): EditOperation = EditOperation(source)
            override fun newArray(size: Int): Array<EditOperation?> = arrayOfNulls(size)
        }
    }

    companion object {
        private const val KEY_UNDO_OPTS = "KEY_UNDO_OPTS"
        private const val KEY_REDO_OPTS = "KEY_REDO_OPTS"

        @JvmStatic
        fun setup(editText: EditText): OperationManager {
            val mgr = OperationManager(editText)
            editText.addTextChangedListener(mgr)
            return mgr
        }
    }
}

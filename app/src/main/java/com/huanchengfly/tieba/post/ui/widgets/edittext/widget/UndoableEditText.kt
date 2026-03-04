package com.huanchengfly.tieba.post.ui.widgets.edittext.widget

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.TextViewCompat
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.widgets.edittext.OperationManager

open class UndoableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    val mgr: OperationManager = OperationManager(this)

    init {
        TextViewCompat.setCustomSelectionActionModeCallback(this, object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.menu_undoable_edit_text, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = false
            override fun onDestroyActionMode(mode: ActionMode) {}
        })
        addTextChangedListener(mgr)
    }

    fun canUndo(): Boolean = mgr.canUndo()

    fun canRedo(): Boolean = mgr.canRedo()

    fun undo(): Boolean = mgr.undo()

    fun redo(): Boolean = mgr.redo()

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(KEY_SUPER, super.onSaveInstanceState())
        bundle.putBundle(KEY_OPT, mgr.exportState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as? Bundle ?: run {
            super.onRestoreInstanceState(state)
            return
        }
        @Suppress("DEPRECATION")
        val superState = bundle.getParcelable<Parcelable>(KEY_SUPER)
        mgr.disable()
        super.onRestoreInstanceState(superState)
        mgr.enable()
        mgr.importState(bundle.getBundle(KEY_OPT))
    }

    companion object {
        private const val KEY_SUPER = "KEY_SUPER"
        private val KEY_OPT = OperationManager::class.java.canonicalName ?: "OperationManager"
    }
}

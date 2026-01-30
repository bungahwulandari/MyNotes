package com.example.mynotes

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mynotes.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NoteDatabase
    private lateinit var adapter: NoteAdapter
    private var allNotes = listOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NoteDatabase.getDatabase(this)

        adapter = NoteAdapter(
            mutableListOf(),
            onEdit = { showEditDialog(it) },
            onDelete = { note ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.noteDao().delete(note)
                    loadNotes()
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(this, "Judul & isi wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                db.noteDao().insert(Note(title = title, content = content))
                loadNotes()
            }

            binding.etTitle.text.clear()
            binding.etContent.text.clear()
        }

        binding.etSearch.addTextChangedListener {
            filterNotes(it.toString())
        }

        loadNotes()
    }

    private fun loadNotes() {
        lifecycleScope.launch(Dispatchers.IO) {
            allNotes = db.noteDao().getAllNotes()
            withContext(Dispatchers.Main) {
                adapter.updateData(allNotes)
            }
        }
    }

    private fun filterNotes(keyword: String) {
        val filtered = allNotes.filter {
            it.title.contains(keyword, true) ||
                    it.content.contains(keyword, true)
        }
        adapter.updateData(filtered)
    }

    private fun showEditDialog(note: Note) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_note, null)
        val etTitle = view.findViewById<EditText>(R.id.etEditTitle)
        val etContent = view.findViewById<EditText>(R.id.etEditContent)

        etTitle.setText(note.title)
        etContent.setText(note.content)

        AlertDialog.Builder(this)
            .setTitle("Edit Catatan")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.noteDao().update(
                        note.copy(
                            title = etTitle.text.toString(),
                            content = etContent.text.toString(),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    loadNotes()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}

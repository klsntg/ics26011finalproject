package com.example.ics26011finalproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HomeFragment : Fragment() {

    private lateinit var libraryAdapter: LibraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize your RecyclerView and BooksAdapter
        val rvLibrary: RecyclerView = view.findViewById(R.id.rvlibrary)
        libraryAdapter = LibraryAdapter({ selectedBook ->
            // Handle remove from library click
            // Example:
            // val removed = DatabaseHandler(requireContext()).removeFromLibrary(selectedBook.id)
            // if (removed) libraryAdapter.setBooks(DatabaseHandler(requireContext()).getLibraryBooks())
            // Notify the adapter about the change
            libraryAdapter.notifyDataSetChanged()
        },{ selectedBook ->
            // Open the BookDetailsFragment with the selected book's details
            val bundle = Bundle().apply {
                putSerializable("selectedBook", selectedBook)
            }

            val bookDetailsFragment = BookDetailsFragment()
            bookDetailsFragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_main, bookDetailsFragment)
                .addToBackStack(null)
                .commit()
        })
        rvLibrary.adapter = libraryAdapter
        rvLibrary.layoutManager = LinearLayoutManager(requireContext())

        // Load data for the library
        val libraryBooks = DatabaseHandler(requireContext()).getLibraryBooks()
        libraryAdapter.setBooks(libraryBooks)

        return view
    }
}
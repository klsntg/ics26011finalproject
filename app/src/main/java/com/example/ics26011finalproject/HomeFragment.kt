package com.example.ics26011finalproject

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HomeFragment : Fragment() {

    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var userEmail: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize your RecyclerView and BooksAdapter
        val rvLibrary: RecyclerView = view.findViewById(R.id.rvlibrary)
        libraryAdapter = LibraryAdapter({ selectedBook ->
            // Handle remove from library click
//            val removed = DatabaseHandler(requireContext()).removeFromLibrary(userEmail, selectedBook.id)
//            if (removed) libraryAdapter.setBooks(DatabaseHandler(requireContext()).getLibraryBooks(userEmail))
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

        // Fetch user's email from SharedPreferences
        userEmail = getUserEmailFromSharedPreferences()

        // Load data for the library
        val libraryBooks = DatabaseHandler(requireContext()).getLibraryBooks(userEmail)
        libraryAdapter.setBooks(libraryBooks)

        return view
    }

    private fun getUserEmailFromSharedPreferences(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("email_address", "") ?: ""
    }
}

package com.example.ics26011finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide

class BookDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_details, container, false)

        // Retrieve selected book details from arguments
        val selectedBook = arguments?.getSerializable("selectedBook") as? Details?

        // Check if selectedBook is not null
        if (selectedBook != null) {
            // Access views in your layout and set their values using selectedBook
            view.findViewById<TextView>(R.id.tvTitle).text = selectedBook.title
            view.findViewById<TextView>(R.id.tvAuthor).text = selectedBook.author
            view.findViewById<TextView>(R.id.tvCategory).text = getCategoryName(selectedBook.categoryId)
            view.findViewById<TextView>(R.id.tvCategory).text = getCategoryName(selectedBook.categoryId)
            view.findViewById<TextView>(R.id.tvRating).text = selectedBook.rating
            view.findViewById<TextView>(R.id.tvPrice).text = selectedBook.price
            view.findViewById<TextView>(R.id.tvDescription).text = selectedBook.description

            view.findViewById<Button>(R.id.btnAddtoLibrary).setOnClickListener {
                // Handle the "Add to Library" button click here
                addToLibrary(selectedBook)
            }

            // Example using Glide for loading the cover image
            Glide.with(requireContext())
                .load(getImageResource(selectedBook.imageSource))
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(view.findViewById<ImageView>(R.id.cover))

            // Inside onCreateView method
            val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

            btnBack.setOnClickListener {
                // Handle the click event here
                parentFragmentManager.popBackStack()
            }
        }


        return view
    }
    private fun addToLibrary(selectedBook: Details) {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Retrieve the user's email from SharedPreferences
        val userEmail = sharedPreferences.getString("email_address", "")


        if (userEmail.isNullOrBlank()) {
            // Handle the case where the user email is not available (e.g., user not logged in)
            Toast.makeText(requireContext(), "User email not available", Toast.LENGTH_SHORT).show()
            return
        }

        val addedSuccessfully = DatabaseHandler(requireContext()).addToLibrary(userEmail, selectedBook.id)

        if (addedSuccessfully) {
            Toast.makeText(requireContext(), "Book added to Library", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Book is already in Library", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getImageResource(imageSource: String): Any {
        return resources.getIdentifier(imageSource, "drawable", requireContext().packageName)
    }
    private fun getCategoryName(categoryId: Int): String {
        // Assuming you have a method to retrieve category name by ID in DatabaseHandler
        return DatabaseHandler(requireContext()).getCategoryNameById(categoryId)
    }
}
package com.namnh.novelreaderapp.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.namnh.novelreaderapp.databinding.FragmentSearchBinding
import com.namnh.novelreaderapp.user_adapter.GenreAdapter


class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val genres = listOf(
        "Võ hiệp", "Huyền Huyễn", "Lãng mạn", "Tiên Hiệp", "Đô Thị", "Hài hước", "Tự do"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RV thể loại
        val genreAdapter = GenreAdapter(genres) { selectedGenre ->
            filterByGenre(selectedGenre)
        }
        binding.rvGenres.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvGenres.adapter = genreAdapter

    }
    private fun filterByGenre(genre: String) {
        val intent = Intent(requireContext(), FilterResultActivity::class.java)
        intent.putExtra("genre", genre)
        startActivity(intent)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package de.fhe.familycare.view.contact.allContacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.fhe.familycare.databinding.FragmentAllContactsBinding
import de.fhe.familycare.view.contact.ContactViewModel
import de.fhe.familycare.view.core.BaseFragment

/**
 * Fragment for all contacts view
 */
class AllContactsFragment : BaseFragment() {

    private var _binding: FragmentAllContactsBinding? = null

    private val binding get() = _binding!!

    private lateinit var allContactsViewModel: ContactViewModel

    /**
     * initializes ViewModel
     * @return binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAllContactsBinding.inflate(inflater, container, false)
        allContactsViewModel = this.getViewModel(ContactViewModel::class.java)
        return binding.root
    }

    /**
     * initializes navController
     * adds OnClickListener to AddContactButton
     * fills AllContactsAdapter with data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        binding.btnAddContact.setOnClickListener{
            val action = AllContactsFragmentDirections.actionMiAllContactsToAddContactFragment()
            navController.navigate(action)
        }

        val adapter = AllContactsAdapter{
            val action = AllContactsFragmentDirections.actionMiAllContactsToShowContact(it.id)
            navController.navigate(action)
        }

        val rvAllContacts = binding.rvAllContacts
        rvAllContacts.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvAllContacts.adapter = adapter

        allContactsViewModel.allContacts.observe(this.viewLifecycleOwner){ contacts ->
            contacts.let {
                adapter.submitList(it)
            }
        }
    }
}
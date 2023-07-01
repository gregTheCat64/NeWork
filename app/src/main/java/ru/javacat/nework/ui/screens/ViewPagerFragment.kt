package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentViewPagerBinding
import ru.javacat.nework.ui.adapter.ViewPagerAdapter
import ru.javacat.nework.ui.viewmodels.AuthViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.util.loadAvatar
import javax.inject.Inject

@AndroidEntryPoint
class ViewPagerFragment: Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    val viewModel: AuthViewModel by viewModels()
    val userViewModel: UserViewModel by viewModels()


    private val fragList = listOf(
        PostsFragment(),
        EventsFragment()
    )

    private val fragTitles = listOf(
        "Записи",
        "Мероприятия"
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentViewPagerBinding.inflate(inflater)

        var currentFragment: Int? = 0
        val avatarImage = binding.topAppBar.findViewById<ImageView>(R.id.appBarImage)
        val defaultAvatar = ResourcesCompat.getDrawable(resources,R.drawable.baseline_account_circle_36, requireActivity().theme)

        avatarImage.setOnClickListener {
            var authorized = viewModel.authorized
            if (authorized) {
                showAuthorizedMenu(it)
            } else showMenu(it)
        }

        //menu:
        viewModel.data.observe(viewLifecycleOwner) {
            val id = appAuth.getId()
            userViewModel.getUserById(id)
            userViewModel.updateFavUserList(id)

        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user.avatar.let {
                val authorized = viewModel.authorized
                if (authorized) {
                    avatarImage.loadAvatar(it.toString())
                } else
                    avatarImage.setImageDrawable(defaultAvatar)
            }
        }



        val adapter = ViewPagerAdapter(requireActivity(), fragList)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager){
                tab,pos-> tab.text = fragTitles[pos]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFragment = tab?.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        binding.addBtn.setOnClickListener {
            if (appAuth.authStateFlow.value.token != null){
                if (currentFragment == 0) {
                    findNavController().navigate(R.id.newPostFragment)
                } else findNavController().navigate(R.id.newEventFragment)
            } else showSignInDialog(this)

        }

        return binding.root
    }

    private fun showMenu(view: View) {
        val menu = PopupMenu(requireContext(), view)
        menu.inflate(R.menu.menu_main)
        menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.signIn -> {
                    findNavController().navigate(R.id.signInFragment)

                }

                R.id.signUp -> {
                    findNavController().navigate(R.id.registrationFragment)

                }

                else -> {
                    Toast.makeText(context, "lala", Toast.LENGTH_SHORT).show()
                }
            }
            true
        })
        menu.show()
    }

    private fun showAuthorizedMenu(view: View) {
        val menu = PopupMenu(requireContext(), view)
        menu.inflate(R.menu.menu_by_authorized)
        menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    showSignOutDialog(appAuth, requireContext())

                }
                R.id.userListBtn -> {
                    findNavController().navigate(R.id.usersSearchFragment)

                }

                R.id.profileBtn -> {
                    if (appAuth.getId() != 0L) {
                        val id = appAuth.getId()
                        val bundle = Bundle()
                        bundle.putLong("userID", id)
                        findNavController().navigate(
                            R.id.wallFragment,
                            bundle
                        )

                    } else findNavController().navigate(R.id.signInFragment)

                }

                else -> {
                    Toast.makeText(context, "lala", Toast.LENGTH_SHORT).show()
                }
            }
            true
        })
        menu.show()
    }
}
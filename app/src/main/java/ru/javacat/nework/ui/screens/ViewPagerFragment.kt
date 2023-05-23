package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentViewPagerBinding
import ru.javacat.nework.ui.adapter.ViewPagerAdapter
import javax.inject.Inject

@AndroidEntryPoint
class ViewPagerFragment: Fragment() {

    private val fragList = listOf(
        PostsFragment(),
        EventsFragment()
    )

    private val fragTitles = listOf(
        "Записи",
        "Мероприятия"
    )

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentViewPagerBinding.inflate(inflater)

        var currentFragment: Int? = 0


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
}
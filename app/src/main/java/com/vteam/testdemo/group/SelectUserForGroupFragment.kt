package com.vteam.testdemo.group

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.adapter.SelectedUserAdapter
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.common.NavigationUtils
import com.vteam.testdemo.databinding.CreateGroupItemLayoutBinding
import com.vteam.testdemo.databinding.SelectUserForGroupFragmentBinding
import com.vteam.testdemo.landing.model.Users
import com.vteam.testdemo.profile.CreateProfileActivity

class SelectUserForGroupFragment : Fragment() {

    companion object {
        fun newInstance() = SelectUserForGroupFragment()
    }


    var selectedUserList: MutableList<Users> = arrayListOf()

    private lateinit var adapter: FirebaseRecyclerAdapter<Users, ContactsViewHolder>
    private lateinit var selectedAdapter: SelectedUserAdapter
    private lateinit var mQuery: Query
    private lateinit var mUsersRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    lateinit var binding: SelectUserForGroupFragmentBinding
    private lateinit var viewModel: CreateGroupViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
        auth = FirebaseAuth.getInstance()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.select_user_for_group_fragment,
                container,
                false
            )
        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolbar as Toolbar?)
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.create_group)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        val horizontalLayoutManager = LinearLayoutManager(context)
        horizontalLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        viewModel = ViewModelProvider(this).get(CreateGroupViewModel::class.java)
        binding.groupList.layoutManager = layoutManager
        binding.groupSelectedList.layoutManager = horizontalLayoutManager
        val mDividerItemDecoration = DividerItemDecoration(
            context,
            layoutManager.orientation
        )
        binding.groupList.addItemDecoration(mDividerItemDecoration)
        auth = FirebaseAuth.getInstance()
        mUsersRef = FirebaseDatabase.getInstance().reference
            .child(Constants.NODES.USER_NODE)
        mQuery = mUsersRef.limitToLast(50)
    }


    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Users>()
            .setQuery(mQuery, Users::class.java)
            .build()

        adapter =
            object : FirebaseRecyclerAdapter<Users, ContactsViewHolder>(options) {
                override fun onBindViewHolder(
                    holder: ContactsViewHolder,
                    position: Int,
                    model: Users
                ) {
                    val visitorUserId = getRef(position).key
                    val name = model.name
                    val profileImage = model.image
                    holder.binding.userProfileName.text = name
                    holder.binding.textStatus.text = model.status
                    if (model.userStatus != null && model.userStatus!!.status != null) {
                        if (model.userStatus!!.status == Constants.USER_STATE.OFFLINE) {
                            holder.binding.usersStatusIcon.background =
                                context!!.getDrawable(R.drawable.live_icon_grey)
                        } else if (model.userStatus!!.status == Constants.USER_STATE.ONLINE) {
                            holder.binding.usersStatusIcon.background =
                                context!!.getDrawable(R.drawable.live_icon_green)
                        }
                    }
                    if (profileImage != null && !profileImage.isEmpty()) {
                        val reference =
                            FirebaseStorage.getInstance().reference
                        reference.child(profileImage).downloadUrl
                            .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                                Log.d("URL", "" + uri)
                                val activity: Activity? = activity
                                if (activity != null) {
                                    Glide.with(activity)
                                        .load(uri)
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(holder.binding.usersProfileImage)
                                }
                            }.addOnFailureListener {
                                // Handle any errors
                            }
                    }
                    holder.binding.root.setOnClickListener {
                        if (model?.isSelected == false) {
                            model?.isSelected = true
                            holder.binding.usersSelectIcon.visibility = View.VISIBLE
                            selectedUserList.add(model)
                            selectedAdapter.notifyItemInserted(selectedUserList.size - 1)


                        } else {
                            model?.isSelected = false
                            holder.binding.usersSelectIcon.visibility = View.INVISIBLE
                            val index = selectedUserList.indexOf(model)
                            selectedUserList.remove(model)
                            selectedAdapter.notifyItemRemoved(index)


                        }
                    }
                }

                override fun onCreateViewHolder(
                    viewGroup: ViewGroup,
                    i: Int
                ): ContactsViewHolder {
                    val binding: CreateGroupItemLayoutBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(viewGroup.context),
                        R.layout.create_group_item_layout,
                        viewGroup,
                        false
                    )
                    return ContactsViewHolder(
                        binding
                    )
                }
            }
        binding.groupList.adapter = adapter
        adapter.startListening()


        selectedAdapter = SelectedUserAdapter(userSelectedList = selectedUserList)
        binding.groupSelectedList.adapter = selectedAdapter
        binding.next.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                activity?.supportFragmentManager?.let {
                    NavigationUtils.addFragment(
                        CreateGroupFragment.newInstance(),
                        NavigationUtils.TransactionType.REPLACE,
                        CreateGroupFragment.javaClass.simpleName,
                        R.id.container,
                        bundle = bundleOf(Constants.KEY.SELECTED_USER to selectedUserList),
                        supportFragmentManager = it
                    )
                }
            }
        })
    }



    override fun onStop() {
        super.onStop()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.create_group_menu, menu)
    }

    class ContactsViewHolder(val binding: CreateGroupItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}
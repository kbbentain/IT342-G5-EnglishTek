import { useState, useEffect } from 'react';
import { User, userService } from '../services/userService';
import DashboardLayout from '../components/DashboardLayout';
import CreateUserModal from '../components/CreateUserModal';
import EditUserModal from '../components/EditUserModal';
import UserDetailsModal from '../components/modals/UserDetailsModal';
import ChangePasswordModal from '../components/modals/ChangePasswordModal';
import Toast from '../components/ui/Toast';
import { UserPlus, Pencil, Trash2, Search, Eye, Lock, ChevronUp, ChevronDown } from 'lucide-react';
import { formatDistanceToNow, parseISO, addHours } from 'date-fns';

type SortField = 'username' | 'name' | 'email' | 'role' | 'createdAt';
type SortOrder = 'asc' | 'desc';

const UserManagement = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [isChangePasswordModalOpen, setIsChangePasswordModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);
  
  // Sorting state
  const [sortField, setSortField] = useState<SortField>('role');
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc');

  useEffect(() => {
    console.log('Initializing UserManagement component');
    fetchUsers();
    fetchCurrentUser();
  }, []);

  const fetchCurrentUser = async () => {
    try {
      const user = await userService.getCurrentUser();
      console.log('Current logged-in user:', {
        id: user.id,
        username: user.username,
        role: user.role,
        email: user.email
      });
      setCurrentUser(user);
    } catch (err) {
      console.error('Failed to fetch current user:', err);
    }
  };

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const data = await userService.getAllUsers();
      console.log('Fetched all users:', data.map(user => ({
        id: user.id,
        username: user.username,
        role: user.role
      })));
      setUsers(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch users');
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (userId: number) => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      console.error('No user found in localStorage');
      return;
    }
    
    const currentUserEmail = JSON.parse(storedUser).email;
    const userToDelete = users.find(user => user.id === userId);

    console.log('Delete operation:', {
      userToDeleteEmail: userToDelete?.email,
      currentUserEmail,
      isCurrentUser: userToDelete?.email === currentUserEmail
    });
    
    // Prevent deleting current user
    if (userToDelete?.email === currentUserEmail) {
      console.log('Prevented deletion of current user');
      setToast({
        message: 'You cannot delete your own account',
        type: 'error'
      });
      return;
    }

    if (!window.confirm('Are you sure you want to delete this user?')) {
      console.log('User cancelled deletion');
      return;
    }

    try {
      console.log('Proceeding with user deletion:', userId);
      await userService.deleteUser(userId);
      setUsers(users.filter(user => user.id !== userId));
      setToast({
        message: 'User deleted successfully',
        type: 'success'
      });
      console.log('Successfully deleted user:', userId);
    } catch (err) {
      console.error('Failed to delete user:', { userId, error: err });
      setToast({
        message: 'Failed to delete user',
        type: 'error'
      });
    }
  };

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortOrder('asc');
    }
  };

  const handleUserUpdated = () => {
    fetchUsers();
    setToast({
      message: 'User updated successfully',
      type: 'success'
    });
  };

  const handlePasswordChanged = () => {
    setToast({
      message: 'Password changed successfully',
      type: 'success'
    });
  };

  const filteredUsers = users.filter(user =>
    user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (user.name?.toLowerCase() || '').includes(searchTerm.toLowerCase())
  );

  // Get current admin email
  const getCurrentAdminEmail = () => {
    try {
      const storedUser = localStorage.getItem('user');
      return storedUser ? JSON.parse(storedUser).email : null;
    } catch (error) {
      console.error('Error getting admin email:', error);
      return null;
    }
  };

  // Sort users
  const sortedUsers = [...filteredUsers].sort((a, b) => {
    const adminEmail = getCurrentAdminEmail();
    
    // Always put current admin first
    if (a.email === adminEmail) return -1;
    if (b.email === adminEmail) return 1;

    let compareA = a[sortField] || '';
    let compareB = b[sortField] || '';

    if (sortField === 'createdAt') {
      compareA = new Date(a.createdAt).getTime();
      compareB = new Date(b.createdAt).getTime();
    }

    if (compareA < compareB) return sortOrder === 'asc' ? -1 : 1;
    if (compareA > compareB) return sortOrder === 'asc' ? 1 : -1;
    return 0;
  });

  // Get current page items
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = sortedUsers.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(sortedUsers.length / itemsPerPage);

  // Change page
  const paginate = (pageNumber: number) => setCurrentPage(pageNumber);

  const renderSortIcon = (field: SortField) => {
    if (sortField !== field) return <ChevronUp className="w-4 h-4 opacity-20" />;
    return sortOrder === 'asc' ? 
      <ChevronUp className="w-4 h-4" /> : 
      <ChevronDown className="w-4 h-4" />;
  };

  const formatRelativeTime = (dateString: string) => {
    // Parse the ISO string and adjust for Asia/Manila timezone (+8)
    const date = addHours(parseISO(dateString), 8);
    return formatDistanceToNow(date, { addSuffix: true });
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {toast && (
          <Toast
            message={toast.message}
            type={toast.type}
            onClose={() => setToast(null)}
          />
        )}
        
        {/* Header */}
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-bold">User Management</h1>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="btn btn-primary"
          >
            <UserPlus className="w-4 h-4 mr-2" />
            Add User
          </button>
        </div>

        {/* Search Bar */}
        <div className="form-control">
          <div className="input-group">
            <label className="input input-bordered flex items-center gap-2 flex-1">
              <Search className="w-4 h-4 opacity-70" />
              <input
                type="text"
                placeholder="Search by username, name, or email..."
                className="grow"
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setCurrentPage(1); // Reset to first page on search
                }}
              />
            </label>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="alert alert-error">
            <span>{error}</span>
          </div>
        )}

        {/* Users Table */}
        <div className="overflow-x-auto rounded-lg border border-base-300 bg-base-100">
          {/* Legend */}
          <div className="p-2 text-sm text-base-content/70 bg-base-200/50 border-b border-base-300 flex items-center gap-2">
            <div className="w-3 h-3 bg-info/20"></div>
            <span>Highlighted row indicates the currently logged-in admin</span>
          </div>
          
          <table className="table table-zebra w-full">
            <thead>
              <tr className="bg-base-200">
                <th 
                  className="font-bold cursor-pointer"
                  onClick={() => handleSort('username')}
                >
                  <div className="flex items-center gap-2">
                    Username
                    {renderSortIcon('username')}
                  </div>
                </th>
                <th 
                  className="font-bold cursor-pointer"
                  onClick={() => handleSort('name')}
                >
                  <div className="flex items-center gap-2">
                    Name
                    {renderSortIcon('name')}
                  </div>
                </th>
                <th 
                  className="font-bold cursor-pointer"
                  onClick={() => handleSort('email')}
                >
                  <div className="flex items-center gap-2">
                    Email
                    {renderSortIcon('email')}
                  </div>
                </th>
                <th 
                  className="font-bold cursor-pointer"
                  onClick={() => handleSort('role')}
                >
                  <div className="flex items-center gap-2">
                    Role
                    {renderSortIcon('role')}
                  </div>
                </th>
                <th 
                  className="font-bold cursor-pointer"
                  onClick={() => handleSort('createdAt')}
                >
                  <div className="flex items-center gap-2">
                    Created
                    {renderSortIcon('createdAt')}
                  </div>
                </th>
                <th className="font-bold text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} className="text-center py-8">
                    <span className="loading loading-spinner loading-lg"></span>
                  </td>
                </tr>
              ) : currentItems.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-8 text-base-content/70">
                    No users found
                  </td>
                </tr>
              ) : (
                currentItems.map((user) => (
                  <tr 
                    key={user.id} 
                    className={`hover border-base-200 ${
                      user.email === getCurrentAdminEmail()
                        ? 'bg-info/20 hover:bg-info/30 font-medium' 
                        : ''
                    }`}
                  >
                    <td className="font-medium">{user.username}</td>
                    <td>{user.name || '-'}</td>
                    <td className="text-base-content/70">{user.email}</td>
                    <td>
                      <span className={`badge ${user.role === 'ADMIN' ? 'badge-primary' : 'badge-secondary'}`}>
                        {user.role}
                      </span>
                    </td>
                    <td className="text-base-content/70">{formatRelativeTime(user.createdAt)}</td>
                    <td>
                      <div className="flex justify-end space-x-2">
                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setIsDetailsModalOpen(true);
                          }}
                          className="btn btn-square btn-sm btn-ghost"
                          title="View Details"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setIsEditModalOpen(true);
                          }}
                          className="btn btn-square btn-sm btn-ghost"
                          title="Edit User"
                        >
                          <Pencil className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => {
                            setSelectedUser(user);
                            setIsChangePasswordModalOpen(true);
                          }}
                          className="btn btn-square btn-sm btn-ghost"
                          title="Change Password"
                        >
                          <Lock className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(user.id)}
                          className={`btn btn-square btn-sm btn-ghost ${
                            user.email === getCurrentAdminEmail()
                              ? 'btn-disabled opacity-50 cursor-not-allowed' 
                              : 'btn-error'
                          }`}
                          title={user.email === getCurrentAdminEmail()
                            ? 'Cannot delete your own account' 
                            : 'Delete User'}
                          disabled={user.email === getCurrentAdminEmail()}
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {!loading && currentItems.length > 0 && (
          <div className="flex justify-between items-center gap-4 px-2">
            <div className="text-sm text-base-content/70">
              Showing {indexOfFirstItem + 1}-{Math.min(indexOfLastItem, sortedUsers.length)} of {sortedUsers.length} users
            </div>
            <div className="join rounded-lg shadow-sm">
              <button
                className="join-item btn btn-sm px-2 min-h-8"
                onClick={() => paginate(1)}
                disabled={currentPage === 1}
              >
                <ChevronUp className="w-4 h-4 -rotate-90" />
                <ChevronUp className="w-4 h-4 -rotate-90 -ml-3" />
              </button>
              <button
                className="join-item btn btn-sm px-3 min-h-8"
                onClick={() => paginate(currentPage - 1)}
                disabled={currentPage === 1}
              >
                <ChevronUp className="w-4 h-4 -rotate-90" />
              </button>
              {Array.from({ length: totalPages }, (_, i) => {
                const pageNum = i + 1;
                // Show first page, last page, current page, and one page before and after current
                if (
                  pageNum === 1 ||
                  pageNum === totalPages ||
                  pageNum === currentPage ||
                  pageNum === currentPage - 1 ||
                  pageNum === currentPage + 1
                ) {
                  return (
                    <button
                      key={pageNum}
                      onClick={() => paginate(pageNum)}
                      className={`join-item btn btn-sm min-h-8 min-w-[2rem] ${
                        currentPage === pageNum
                          ? 'btn-primary text-primary-content'
                          : ''
                      }`}
                    >
                      {pageNum}
                    </button>
                  );
                } else if (
                  pageNum === currentPage - 2 ||
                  pageNum === currentPage + 2
                ) {
                  return (
                    <button
                      key={pageNum}
                      className="join-item btn btn-sm min-h-8 min-w-[2rem] btn-disabled"
                    >
                      ...
                    </button>
                  );
                }
                return null;
              })}
              <button
                className="join-item btn btn-sm px-3 min-h-8"
                onClick={() => paginate(currentPage + 1)}
                disabled={currentPage === totalPages}
              >
                <ChevronUp className="w-4 h-4 rotate-90" />
              </button>
              <button
                className="join-item btn btn-sm px-2 min-h-8"
                onClick={() => paginate(totalPages)}
                disabled={currentPage === totalPages}
              >
                <ChevronUp className="w-4 h-4 rotate-90" />
                <ChevronUp className="w-4 h-4 rotate-90 -ml-3" />
              </button>
            </div>
            <div className="text-sm text-base-content/70 min-w-[100px]">
              Page {currentPage} of {totalPages}
            </div>
          </div>
        )}

        {/* Modals */}
        {isCreateModalOpen && (
          <CreateUserModal
            isOpen={isCreateModalOpen}
            onClose={() => setIsCreateModalOpen(false)}
            onUserCreated={() => {
              fetchUsers();
              setToast({
                message: 'User created successfully',
                type: 'success'
              });
            }}
          />
        )}

        {isEditModalOpen && selectedUser && (
          <EditUserModal
            isOpen={isEditModalOpen}
            onClose={() => setIsEditModalOpen(false)}
            user={selectedUser}
            onUserUpdated={handleUserUpdated}
          />
        )}

        {isDetailsModalOpen && selectedUser && (
          <UserDetailsModal
            isOpen={isDetailsModalOpen}
            onClose={() => setIsDetailsModalOpen(false)}
            user={selectedUser}
          />
        )}

        {isChangePasswordModalOpen && selectedUser && (
          <ChangePasswordModal
            isOpen={isChangePasswordModalOpen}
            onClose={() => setIsChangePasswordModalOpen(false)}
            user={selectedUser}
            onPasswordChanged={handlePasswordChanged}
          />
        )}
      </div>
    </DashboardLayout>
  );
};

export default UserManagement;

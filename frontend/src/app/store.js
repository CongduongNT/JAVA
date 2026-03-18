import { configureStore } from '@reduxjs/toolkit'
import authReducer from '../features/auth/authSlice'
import usersReducer from '../features/users/usersSlice'
import settingsReducer from '../features/settings/settingsSlice'

export const store = configureStore({
  reducer: {
    auth:     authReducer,
    users:    usersReducer,
    settings: settingsReducer,
  },
})

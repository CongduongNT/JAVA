import { configureStore } from '@reduxjs/toolkit'
import authReducer from '../features/auth/authSlice'
import usersReducer from '../features/users/usersSlice'
import settingsReducer from '../features/settings/settingsSlice'
import promptTemplatesReducer from '../features/promptTemplates/promptTemplateSlice'
import questionBankReducer from '../features/question-bank/questionBankSlice'
import lessonPlansReducer from '../features/lesson-plans/lessonPlanSlice'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    users: usersReducer,
    settings: settingsReducer,
    promptTemplates: promptTemplatesReducer,
    questionBank: questionBankReducer,
    lessonPlans: lessonPlansReducer,
  },
})

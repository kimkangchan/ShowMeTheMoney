export type TransactionType = "INCOME" | "EXPENSE";

export type ExpenseCategoryCode =
  | "FOOD"
  | "CAFE"
  | "TRANSPORT"
  | "SHOPPING"
  | "HOUSING"
  | "MEDICAL"
  | "CULTURE"
  | "EDUCATION"
  | "ETC";

export type IncomeCategoryCode = "SALARY" | "INVESTMENT" | "ETC" | "PART_TIME";

export type CategoryCode = ExpenseCategoryCode | IncomeCategoryCode;

export interface Category {
  uuid: number;
  type: TransactionType;
  code: CategoryCode;
  name: string;
}

export interface User {
  uuid: number;
  username: string;
  name: string;
  email: string;
  grade: 0 | 1;
  createdAt: string;
}

export interface Transaction {
  uuid: number;
  uuidUser: number;
  type: TransactionType;
  amount: number;
  uuidCategory: number;
  categoryName?: string;
  categoryCode?: CategoryCode;
  memo?: string;
  transactionAt: string;
  createdAt: string;
  updatedAt?: string;
}

export interface RecurringItem {
  uuid: number;
  uuidUser: number;
  uuidCategory: number;
  categoryName?: string;
  type: TransactionType;
  name: string;
  amount: number;
  billingDay: number;
  isActive: 0 | 1;
  createdAt: string;
}

export interface Budget {
  uuid: number;
  uuidUser: number;
  yearMonth: number;
  amount: number;
  memo?: string;
  createdAt: string;
}

export interface DashboardSummary {
  yearMonth: number;
  totalIncome: number;
  totalExpense: number;
  balance: number;
  budgetAmount: number | null;
  budgetUsageRate: number | null;
  isOverBudget: boolean;
}

export interface CategoryExpense {
  categoryCode: CategoryCode;
  categoryName: string;
  amount: number;
  ratio: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

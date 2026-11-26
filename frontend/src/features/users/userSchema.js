import { z } from 'zod'

export const userSchema = z.object({
  firstName: z
    .string()
    .trim()
    .min(2, 'Le prénom doit comporter au moins 2 caractères')
    .max(100, 'Le prénom ne peut pas dépasser 100 caractères'),
  lastName: z
    .string()
    .trim()
    .min(2, 'Le nom doit comporter au moins 2 caractères')
    .max(100, 'Le nom ne peut pas dépasser 100 caractères'),
  email: z
    .string()
    .trim()
    .min(1, "L'adresse email est requise")
    .email('Adresse email invalide')
    .max(180, "L'adresse email ne peut pas dépasser 180 caractères"),
  password: z
    .string()
    .min(8, 'Le mot de passe doit comporter au moins 8 caractères')
    .max(100, 'Le mot de passe ne peut pas dépasser 100 caractères')
    .regex(/[A-Z]/, 'Le mot de passe doit contenir au moins une majuscule')
    .regex(/\d/, 'Le mot de passe doit contenir au moins un chiffre'),
  role: z.enum(['ADMIN', 'STUDENT', 'SUPERVISOR'], {
    errorMap: () => ({ message: 'Veuillez sélectionner un rôle valide' }),
  }),
})

export default userSchema

import type React from "react"
import type { Metadata } from "next"
import { Inter } from "next/font/google"
import "./globals.css"
import { Navbar } from "@/components/navbar"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "sonner" // ✅ AJOUT

const inter = Inter({ subsets: ["latin"] })

export const metadata: Metadata = {
  title: "Smart Ticket - Outil intelligent de traitement des signalements",
  description:
    "Système centralisé et intelligent pour collecter, traiter et gérer les signalements utilisateurs avec l'aide de l'IA",
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="fr">
      <body className={inter.className}>
        <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
          <Navbar />
          <main>{children}</main>
          <Toaster richColors position="top-right" /> {/* ✅ AJOUT ICI */}
        </ThemeProvider>
      </body>
    </html>
  )
}

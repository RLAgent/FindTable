UPDATE restaurant_data SET open_tables_X4 = 20-ABS(20-ABS(open_tables_X4 + FLOOR(RAND()*(1+2/35-2/175)-2/70) )), open_tables_Y2 = 20-ABS(20-ABS(open_tables_Y2 + FLOOR(RAND()*(1+2/70-2/350)-2/140) )),open_tables_Z3 = 20-ABS(20-ABS(open_tables_Z3 + FLOOR(RAND()*(1+2/70-2/350)-2/140) )) WHERE has_table_data="True" AND restaurant_id!=66441

 \begin{itemize}
                    \item \texttt{restaurant\_id}: The \textsc{uid} our application gives the restaurant
                    \item \texttt{id}: The \textsc{uid} Yelp gives the restaurant
                    \item \texttt{name}: The name of the restaurant
                    \item \texttt{type}: The restaurant categories (e.g., Japanese, Mediterranean, Barbecue)
                    \item \texttt{location\_address}: The street address of the restaurant
                    \item \texttt{location\_city}: The city of the restaurant
                    \item \texttt{location\_state\_code}: The state code of the restaurant
                    \item \texttt{location\_postal\_code}: The postal code of the restaurant
                    \item \texttt{waitlist\_length}:
                    \item \texttt{open\_tables\_X\{2..20\}}:
                    \item \texttt{open\_tables\_Y\{2..20\}}:
                    \item \texttt{open\_tables\_Z\{2..20\}}:
                    \item \texttt{open\_tables\_BAR}:
                \end{itemize}
                
     \begin{itemize}
                    \item \texttt{restaurant\_id}: The restaurant \textsc{id}
                    \item \texttt{username}: The username credential for the restaurant
                    \item \texttt{password}: The password credential for the restaurant
                    \item \texttt{token}: The valid authentication token of the restaurant
                \end{itemize}
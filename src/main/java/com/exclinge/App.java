package com.exclinge;

import java.util.ArrayList;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.*;
import microsoft.exchange.webservices.data.core.enumeration.misc.*;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.definition.PropertyDefinition;
import microsoft.exchange.webservices.data.search.CalendarView;
import microsoft.exchange.webservices.data.search.FindItemsResults;

/**
 * Hello world!
 *
 */
public class App
{
    static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl
    {
        public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl)
        {
            return redirectionUrl.toLowerCase().startsWith("https://");
        }
    }

    /*static String[][] digits =
    {
    {
     "###",
     "# #",
     "# #",
     "# #",
     "###",
    },
    {
     " # ",
     " # ",
     " # ",
     " # ",
     " # ",
    },
    {
     "###",
     "  #",
     "###",
     "#  ",
     "###"
    },
    {
     "###",
     "  #",
     "###",
     "  #",
     "###"
    },
    {
     "# #",
     "# #",
     "###",
     "  #",
     "  #"
    },
    {
     "###",
     "#  ",
     "###",
     "  #",
     "###"
    },
    {
     "###",
     "#  ",
     "###",
     "# #",
     "###"
    },
    {
     "###",
     "  #",
     "  #",
     "  #",
     "  #"
    },
    {
     "###",
     "# #",
     "###",
     "# #",
     "###"
    },
    {
     "###",
     "# #",
     "###",
     "  #",
     "###"
    },
    };*/
    static String[][] digits =
    {
    {
     "#####",
     "#   #",
     "#   #",
     "#   #",
     "#####",
    },
    {
     "  #  ",
     "  #  ",
     "  #  ",
     "  #  ",
     "  #  ",
    },
    {
     "#####",
     "    #",
     "#####",
     "#    ",
     "#####"
    },
    {
     "#####",
     "    #",
     "#####",
     "    #",
     "#####"
    },
    {
     "#   #",
     "#   #",
     "#####",
     "    #",
     "    #"
    },
    {
     "#####",
     "#    ",
     "#####",
     "    #",
     "#####"
    },
    {
     "#####",
     "#    ",
     "#####",
     "#   #",
     "#####"
    },
    {
     "#####",
     "    #",
     "    #",
     "    #",
     "    #"
    },
    {
     "#####",
     "#   #",
     "#####",
     "#   #",
     "#####"
    },
    {
     "#####",
     "#   #",
     "#####",
     "    #",
     "#####"
    },
    };

    static class PrintAppointments extends TimerTask {
        ExchangeService service;

        int counter;
        String appointmentString;

        PrintAppointments(ExchangeService service)
        {
            this.service = service;
            counter = 20;
        }

        private String asciiDate(Date date)
        {
            String code = "";
            for (int line = 0; line < 5; ++line)
            {
                String sep = "  " + (line%2 == 1 ? "*" : " ") + "  ";
                code += digits[date.getHours()/10][line] + " " + digits[date.getHours()%10][line];
                code += sep + digits[date.getMinutes()/10][line] + " " + digits[date.getMinutes()%10][line];
                code += sep + digits[date.getSeconds()/10][line] + " " + digits[date.getSeconds()%10][line];
                if (line < 4)
                    code += "\n";
            }
            return code;
        }

        private void findAppointments()
        {
            Date nowDate = new Date();
            DateFormat formatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMANY);
            System.out.print("\033[2J" + asciiDate(nowDate) +"\n\n\n"+appointmentString);
            counter++;
            if (counter < 20)
            {
                counter = 0;
                return;
            }
            try
            {
                Calendar startCal = Calendar.getInstance();
                Calendar endCal = Calendar.getInstance();
                startCal.set(Calendar.HOUR_OF_DAY,0);
                startCal.set(Calendar.MINUTE,0);
                startCal.set(Calendar.SECOND,0);
                endCal.set(Calendar.HOUR_OF_DAY,23);
                endCal.set(Calendar.MINUTE,59);
                endCal.set(Calendar.SECOND,59);
                Date startDate = startCal.getTime();
                Date endDate = endCal.getTime();
                CalendarFolder cf = CalendarFolder.bind(service, WellKnownFolderName.Calendar);
                FindItemsResults<Appointment> findResults = cf.findAppointments(new CalendarView(startDate, endDate));
                appointmentString = "";
                for (Appointment appt : findResults.getItems()) {
                    if (appt.getEnd().before(startDate))
                        continue;
                    Date remDate = Date.from(appt.getStart().toInstant().minusSeconds(15*60));
                    Map<PropertyDefinition,Object> properties = appt.getPropertyBag().getProperties();
                    String colorCode = appt.getEnd().before(nowDate) ? "\033[38;5;235m" : (appt.getStart().after(nowDate) ? (remDate.before(nowDate) ?  "\033[38;5;227m" : "\033[38;5;255m") : "\033[30m\033[48;5;227m");
                    appointmentString += colorCode + formatter.format(appt.getStart()) + " - " + formatter.format(appt.getEnd()) + "    " + appt.getSubject() + "\033[49m\033[39m\n";
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.err.println(e);
                return;
            }
        }

        public void run() {
            findAppointments();
        }
    }



    public static void main( String[] args )
    {
        System.out.print("Enter your e-mail address: ");
        String email = System.console().readLine();
        System.out.print("Enter your user name: ");
        String user = System.console().readLine();
        System.out.print("Enter your password: ");
        String pw = new String(System.console().readPassword());
        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials(user, pw);
        service.setCredentials(credentials);
        try
        {
            service.autodiscoverUrl(email, new RedirectionUrlCallback());
            Timer timer = new Timer();
            timer.schedule(new PrintAppointments(service), 0, 500);
            while (true)
            {
                try
                {
                    Thread.sleep(50);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println(e);
            return;
        }
    }
}

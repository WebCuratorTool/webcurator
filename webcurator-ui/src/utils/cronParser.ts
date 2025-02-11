import { Cron } from "croner";
import { formatTime } from "./helper";

export const days = [
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
];

export const dates = [
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    "10",
    "11",
    "12",
    "13",
    "14",
    "15",
    "16",
    "17",
    "18",
    "19",
    "20",
    "21",
    "22",
    "23",
    "24",
    "25",
    "26",
    "27",
    "28",
    "29",
    "30",
    "31",
    "Last"
]

export const months = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
];


export const parseCron = (cronString: string) => {
    const parts = cronString.split(" ");
    const cron = {
      minute: parts[0],
      hour: parts[1],
      dayOfMonth: parts[2],
      month: parts[3],
      dayOfWeek: parts[4],
      year: parts[5]
    };

    return cron;
}

export const createCronExpression = (cronSettings: {dayOfMonth: string, months: string, dayOfWeek: string, time: number}) => {
    console.log(cronSettings);
    const cron = {
        minute: "",
        hour: "",
        dayOfMonth: "",
        month: "",
        dayOfWeek: "",
    };

    let time;

    // Get the hour and minute from the time
    // It may be a date object if selected by the time picket, otherwise it's a number
    if (Object.prototype.toString.call(cronSettings.time) === '[object Date]') {
        time = formatTime(cronSettings.time).split(":");
    } else time = cronSettings.time.toString().split(":");

    cron.hour = time[0];
    cron.minute = time[1];

    // Get the day of month or set to '?' if not specified
    if (cronSettings.dayOfMonth) {
        cron.dayOfMonth = cronSettings.dayOfMonth === "Last" ? "L" : cronSettings.dayOfMonth;
    } else {
        cron.dayOfMonth = "?";
    }
    
    // Get the month or set to '*' if not specified
    if (cronSettings.months) {
        const cronMonths = cronSettings.months.split(", ");

        // If there's only one month, set it directly
        if (cronMonths.length === 1) {
            cron.month = (months.indexOf(cronMonths[0]) + 1).toString();
        // If there's more than one month, find the interval 
        } else {
            // Get the index/number of each month
            const monthNumbers = cronMonths.map((month: string) => months.indexOf(month) + 1);

            const firstMonth = Math.min(...monthNumbers); // Find the smallest month number in the selection
            const lastMonth = Math.max(...monthNumbers); // Find the largest month number in the selection
    
            const interval = (lastMonth - firstMonth) / (monthNumbers.length - 1); // Calculate the interval between the months
            cron.month = `${firstMonth}/${interval}`; // Set the cron month with the first month and interval
        }
    } else {
        cron.month = "*";
    }

    if (cronSettings.dayOfWeek) {
        cron.dayOfWeek = cronSettings.dayOfWeek.slice(0, 3).toUpperCase();
    } else {
        cron.dayOfWeek = "?";
    }

    console.log(`${cron.minute} ${cron.hour} ${cron.dayOfMonth} ${cron.month} ${cron.dayOfWeek} *`);
    return `${cron.minute} ${cron.hour} ${cron.dayOfMonth} ${cron.month} ${cron.dayOfWeek} *`;
}

export const getCronMonths = (cronPattern: string) => {
    const cronParts = parseCron(cronPattern);
    const monthPart = cronParts.month;

    if (monthPart.includes("/")) {
        const interval = parseInt(monthPart.split("/")[1]);
        const startMonthIndex = parseInt(monthPart.split("/")[0]) - 1;
        let monthsList = "";

        for (let i = startMonthIndex; i < months.length; i += interval) {
            monthsList += months[i] + ", ";
        }

        return monthsList.slice(0, -2);
    } else {
        return months[Number(monthPart) - 1];
    }
}

export const quartzToUnix = (quartzExpression: string): string => {
    const parts = parseCron(quartzExpression);

    let unixDom = parts.dayOfMonth;
    let unixDow = parts.dayOfWeek;

    // If Quartz was '?', set unix to '*'
    if (parts.dayOfMonth === '?') unixDom = '*';
    if (parts.dayOfWeek === '?') unixDow = '*';

    return `${parts.minute} ${parts.hour} ${unixDom} ${parts.month} ${unixDow}`;
}

export const getNextScheduledTimes = (cronString: string, nextRuns: number): Date[] => {    
    const unixExpression = quartzToUnix(cronString);
    
    const cron = new Cron(unixExpression);
    const nextTimes = cron.nextRuns(nextRuns);

    return nextTimes;
};

export const getMonthGroups = (type: string) => {
    switch (type.toLowerCase()) {
        case 'bimonthly':
            return getAlternatingMonths();
        case 'quarterly':
            return ['January, April, July, October', 'February, May, August, November', 'March, June, September, December'];
        case 'half-yearly':
            return ['January, July', 'February, August', 'March, September', 'April, October', 'May, November', 'June, December'];
        case 'annually':
        default:
            return months;
    }
}

export const getAlternatingMonths = () => {
    const oddMonths = months.filter((month, index) => index % 2 === 0).join(', ');
    const evenMonths = months.filter((month, index) => index % 2 !== 0).join(', ');
  
    return [ oddMonths, evenMonths ];
}

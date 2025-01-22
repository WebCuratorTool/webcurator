import { Cron } from "croner";

export const days = [
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
];

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

export const getNextScheduledTimes = (cronString: string): Date[] => {    
    const unixExpression = quartzToUnix(cronString);
    
    const cron = new Cron(unixExpression);
    const nextTimes = cron.nextRuns(10);

    return nextTimes;
};


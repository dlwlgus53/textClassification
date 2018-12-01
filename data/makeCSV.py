import csv


data = []
test_data = []


xmlFile = 'reviewData.xml'
xmlData = open(xmlFile, 'w')
xmlData.write('<short_desk>\n')


#review.csv  0number-1product-2user-3profile-4help!-5nothelp+help!-6score-7time-8summary-9text
#text.csv 0number-1score-2summary-text

#2user-1product-6score-4help-5denominator-8summary-9text-test

with open('Reviews.csv', mode='r') as review_file:
    review_reader = csv.reader(review_file)
    line_count = 0
    for row in review_reader:
        if line_count == 0:
            fieldname = [id, row[0], row[1] ]
            test_fieldname = [id, row[0], row[1],]
            line_count += 1
        else:
            if line_count%2==0:
                
                xmlData.write("<report id =\"" + str(line_count/2) + "\">\n")
                xmlData.write(" <summary>"+row[8]+"</summary>\n")
                xmlData.write(" <score>"+row[6]+"</score>\n")
                xmlData.write("</report>\n")
            line_count += 1
    
    xmlData.write("</short_desk>")


    
